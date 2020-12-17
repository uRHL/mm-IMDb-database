import json
from pathlib import Path

inputFile = "../output/spanish-word-dic.json"
outputFile = "./queries/query4.json"
string_query = ""
counter = 0

query_obj = {
    "query": {
        "bool": {
            "must": [
                {"query_string": {
                    "fields": ["title^2", "synopsis", "plotKeywords^3"],
                    "query": ""}
                },
                {"range": {"releaseYear": {"gte": 1950}}

                 }
            ]
        }
    }
}

if (Path(outputFile).is_file()) == False:
    # the query request has not been created yet. Create it
    try:
        with open(inputFile, "r") as reader:

            wordList = json.loads(reader.read())
            print(len(wordList))
            for elem in wordList:
                if counter == (len(wordList) - 1):
                    nextClause = ""
                else:
                    nextClause = " OR "
                word = elem.replace("'", "").replace(",", "").strip()
                string_query += "(" + word + ")" + nextClause
                counter += 1
            # end for loop
            query_obj["query"]["bool"]["must"][0]["query_string"]["query"] = string_query
            with open(outputFile, "w") as writer:
                writer.write(json.dumps(query_obj, indent=4))
    finally:
        writer.close()
        reader.close()
# end of the script
