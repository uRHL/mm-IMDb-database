import json
from pathlib import Path

inputFile = "../output/spanish-word-dic.json"
outputFile = "./queries/query4.json"
query_words = ""
WORDS_PER_STRING_QUERY = 250
counter = 0

query_obj = {
    "query": {
        "bool": {
            "should": []
        }
    }
}

if (Path(outputFile).is_file()) == False:
    # the query request has not been created yet. Create it
    try:
        with open(inputFile, "r") as reader:

            wordList = json.loads(reader.read())

            for elem in wordList:
                if counter == (len(wordList) - 1) or (counter % WORDS_PER_STRING_QUERY == WORDS_PER_STRING_QUERY - 1):
                    nextClause = ""
                else:
                    nextClause = " OR "
                if (counter % WORDS_PER_STRING_QUERY == 0) and (counter > 0):
                    # Every WORDS_PER_STRING_QUERY words a new query string is added to the bool query
                    new_query_string = {
                        "query_string": {
                            "fields": ["title", "synopsis", "plotKeywords", "filmingLocations", "originCountry",
                                       "primaryLanguages"],
                            "query": ""
                        }
                    }
                    print(query_words)
                    new_query_string["query_string"]["query"] = query_words
                    query_obj["query"]["bool"]["should"].append(new_query_string)
                    query_words = ""
                # read the next word
                word = elem.replace("'", "").replace(",", "").strip()
                query_words += "(" + word + ")" + nextClause
                counter += 1
            # end for loop

            # Add the remaining words to the query, if any
            if (len(query_words) > 0):
                new_query_string = {
                    "query_string": {
                        "fields": ["title", "synopsis", "plotKeywords", "filmingLocations", "originCountry",
                                   "primaryLanguages"],
                        "query": ""
                    }
                }
                new_query_string["query_string"]["query"] = query_words
                query_obj["query"]["bool"]["should"].append(new_query_string)

            # generate the json request
            with open(outputFile, "w") as writer:
                writer.write(json.dumps(query_obj, indent=4))
    finally:
        writer.close()
        reader.close()
# end of the script
