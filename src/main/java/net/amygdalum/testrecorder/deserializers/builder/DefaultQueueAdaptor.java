package net.amygdalum.testrecorder.deserializers.builder;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.deserializers.Templates.assignLocalVariableStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.callMethodStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.newObject;
import static net.amygdalum.testrecorder.util.Types.baseType;
import static net.amygdalum.testrecorder.util.Types.equalTypes;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;

import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.deserializers.DeserializerContext;
import net.amygdalum.testrecorder.deserializers.TypeManager;
import net.amygdalum.testrecorder.values.SerializedList;

public class DefaultQueueAdaptor extends DefaultSetupGenerator<SerializedList> implements SetupGenerator<SerializedList> {

    @Override
    public Class<SerializedList> getAdaptedClass() {
        return SerializedList.class;
    }

    @Override
    public boolean matches(Type type) {
        return Queue.class.isAssignableFrom(baseType(type));
    }

    @Override
    public Computation tryDeserialize(SerializedList value, SetupGenerators generator, DeserializerContext context) {
        TypeManager types = generator.getTypes();
        Type type = value.getType();
        Type resultType = value.getResultType();
        types.registerTypes(resultType, type);

        return generator.forVariable(value, Queue.class, local -> {

            List<Computation> elementTemplates = value.stream()
                .map(element -> element.accept(generator))
                .filter(element -> element != null)
                .collect(toList());

            List<String> elements = elementTemplates.stream()
                .map(template -> generator.adapt(template.getValue(), value.getComponentType(), template.getType()))
                .collect(toList());

            List<String> statements = elementTemplates.stream()
                .flatMap(template -> template.getStatements().stream())
                .collect(toList());

            Type effectiveResultType = types.isHidden(resultType) ? Queue.class : resultType;
            Type temporaryType = (!types.isHidden(type) && Queue.class.isAssignableFrom(baseType(type)))
                ? type
                : Queue.class.isAssignableFrom(baseType(effectiveResultType))
                    ? effectiveResultType
                    : Queue.class;

            String tempVar = local.getName();
            if (!equalTypes(effectiveResultType, temporaryType)) {
                tempVar = generator.temporaryLocal();
            }

            String list = types.isHidden(type) ? generator.adapt(types.getWrappedName(type), temporaryType, types.wrapHidden(type)) : newObject(types.getBestName(type));
            String listInit = assignLocalVariableStatement(types.getRelaxedName(temporaryType), tempVar, list);
            statements.add(listInit);

            for (String element : elements) {
                String addElement = callMethodStatement(tempVar, "add", element);
                statements.add(addElement);
            }

            if (generator.needsAdaptation(effectiveResultType, temporaryType)) {
                tempVar = generator.adapt(tempVar, effectiveResultType, temporaryType);
                statements.add(assignLocalVariableStatement(types.getRelaxedName(effectiveResultType), local.getName(), tempVar));
            } else if (!equalTypes(effectiveResultType, temporaryType)) {
                statements.add(assignLocalVariableStatement(types.getRelaxedName(effectiveResultType), local.getName(), tempVar));
            }

            return new Computation(local.getName(), effectiveResultType, true, statements);
        });
    }

}
