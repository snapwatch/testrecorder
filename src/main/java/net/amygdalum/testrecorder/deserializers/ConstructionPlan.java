package net.amygdalum.testrecorder.deserializers;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.deserializers.Templates.assignLocalVariableStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.callMethodStatement;
import static net.amygdalum.testrecorder.deserializers.Templates.newObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;

public class ConstructionPlan implements Comparable<ConstructionPlan> {

    private LocalVariable var;
    private ConstructorParams constructorParams;
    private List<SetterParam> setterParams;

    public ConstructionPlan(LocalVariable var, ConstructorParams constructorParams, List<SetterParam> setterParams) {
        this.var = var;
        this.constructorParams = constructorParams;
        this.setterParams = setterParams;
    }

    public ConstructionPlan disambiguate(Collection<Constructor<?>> constructors) {
        for (Constructor<?> constructor : constructors) {
            if (constructorParams.hasAmbiguitiesWith(constructor)) {
                constructorParams.insertTypeCasts();
            }
        }
        return this;
    }

    public Object execute() {
        try {
            Object base = constructorParams.apply();
            for (SetterParam param : setterParams) {
                param.apply(base);
            }
            return base;
        } catch (Exception e) {
            return null;
        }
    }

    public Computation compute(TypeManager types, SetupGenerators generator) {
        Class<?> clazz = constructorParams.getType();
        types.registerTypes(clazz);

        List<String> statements = new ArrayList<>();

        List<Computation> computedParams = constructorParams.getParams().stream()
            .map(value -> value.compile(types, generator))
            .collect(toList());

        statements.addAll(computedParams.stream()
            .flatMap(computation -> computation.getStatements().stream())
            .collect(toList()));

        String[] params = computedParams.stream()
            .map(computation -> computation.getValue())
            .toArray(String[]::new);

        String bean = newObject(types.getBestName(clazz), params);
        String constructorStatement = assignLocalVariableStatement(types.getBestName(clazz), var.getName(), bean);
        statements.add(constructorStatement);
        var.define(clazz);

        for (SetterParam param : setterParams) {
            Computation fieldComputation = param.computeSerializedValue().accept(generator);
            statements.addAll(fieldComputation.getStatements());

            String setStatement = callMethodStatement(var.getName(), param.getName(), fieldComputation.getValue());
            statements.add(setStatement);
        }

        return new Computation(var.getName(), null, true, statements);
    }

    @Override
    public int compareTo(ConstructionPlan o) {
        int constructorSize = constructorParams.size();
        int oconstructorSize = o.constructorParams.size();
        int setterSize = setterParams.size();
        int osetterSize = o.setterParams.size();

        int size = constructorSize + setterSize;
        int osize = oconstructorSize + osetterSize;

        int compare = size - osize;
        if (compare == 0) {
            compare = oconstructorSize - constructorSize;
        }
        if (compare == 0) {
            compare = System.identityHashCode(this) - System.identityHashCode(o);
        }
        return compare;
    }

}
