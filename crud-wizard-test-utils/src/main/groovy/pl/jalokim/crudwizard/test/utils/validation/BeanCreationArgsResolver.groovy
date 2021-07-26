package pl.jalokim.crudwizard.test.utils.validation

class BeanCreationArgsResolver {

    static def createInstance(Class<?> typeInstance, List<Object> argumentsForInject) {
        def firstConstructor = typeInstance.getConstructors().first()
        def constrParameterTypes = firstConstructor.getParameterTypes()
        if (constrParameterTypes.length == 0) {
            return firstConstructor.newInstance()
        }
        List<Object> foundArguments = []
        constrParameterTypes.each {constrParameterType ->
            Object foundArgument
            argumentsForInject.each {
                if (foundArgument == null && it.class == constrParameterType) {
                    foundArgument = it
                }
            }

            if (foundArgument == null) {
                argumentsForInject.each {
                    if (foundArgument == null && constrParameterType.isAssignableFrom(it.class)) {
                        foundArgument = it
                    }
                }
            }
            if (foundArgument == null) {
                throw new IllegalArgumentException("Cannot find constructor argument with type: ${constrParameterType.getName()} " +
                    "for class: ${typeInstance.getName()} in provided dependencies: ${argumentsForInject}")
            }
            foundArguments.add(foundArgument)
        }
        return firstConstructor.newInstance(foundArguments.toArray())
    }
}
