package pl.jalokim.crudwizard.test.utils.random

import com.github.javafaker.Faker

class DataFakerHelper {

    private static final Faker FAKER = Faker.instance(new Random(10))

    static Faker getFaker() {
        return FAKER
    }

    static String randomText(int fixedNumberOfCharacters = 15) {
        return getFaker().lorem().characters(fixedNumberOfCharacters)
    }

    static String randomNumbersAsText(int numberLength = 2) {
        return getFaker().numerify("#"*(numberLength))
    }

    static Long randomLong(Long max = Long.MAX_VALUE) {
        return getFaker().random().nextLong(max)
    }

    static Integer randomInteger(Integer max = Integer.MAX_VALUE) {
        return getFaker().random().nextInt(max)
    }
}
