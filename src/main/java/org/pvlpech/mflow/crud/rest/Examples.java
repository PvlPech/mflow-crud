package org.pvlpech.mflow.crud.rest;

final class Examples {
    private Examples() {
    }

    static final String VALID_EXAMPLE_USER_TO_CREATE = """
        {
            "name": "Paul"
        }
        """;

    static final String VALID_EXAMPLE_USER = """
		{
			"id": 1,
			"name": "Paul"
		}
		""";

    static final String VALID_EXAMPLE_USER_LIST = "[" + VALID_EXAMPLE_USER + "]";

    static final String VALID_EXAMPLE_CURRENCY = """
        {
        	"id": 784,
            "code": "AED",
            "name": "Дирхам (ОАЭ)"
        }
        """;

    static final String VALID_EXAMPLE_CURRENCY_LIST = "[" + VALID_EXAMPLE_CURRENCY + "]";

    static final String VALID_EXAMPLE_GROUP_TO_CREATE = """
        {
            "name": "Family",
            "owner": {
                "id": 1
            }
        }
        """;

    static final String VALID_EXAMPLE_GROUP = """
        {
        	"id": 1,
            "name": "Family",
            "owner": """ + VALID_EXAMPLE_USER + """
        }
        """;

    static final String VALID_EXAMPLE_GROUP_LIST = "[" + VALID_EXAMPLE_GROUP + "]";

    static final String VALID_EXAMPLE_CATEGORY_TO_CREATE = """
        {
            "name": "Taxi",
            "group": {
                "id": 1
            }
        }
        """;

    static final String VALID_EXAMPLE_CATEGORY = """
        {
        	"id": 1,
            "name": "Taxi",
            "group": """ + VALID_EXAMPLE_GROUP + """
        }
        """;

    static final String VALID_EXAMPLE_CATEGORY_LIST = "[" + VALID_EXAMPLE_CATEGORY + "]";
}