import json
from pathlib import Path

inputFile = "../output/spanish-word-dic.json"
outputFile = "./queries/query4.json"
query_words = ""
counter = 0

query_obj = {
    "size": 10,
    "query": {
        "query_string": {
            "fields": ["title^2", "synopsis", "plotKeywords^4", "filmingLocations^2", "originCountry^2",
                        "primaryLanguages^2"],
            "query": ""
        }
    }
}

if (Path(outputFile).is_file()) == False:
    # the query request has not been created yet. Create it
    try:
        with open(inputFile, "r") as reader:

            wordList = json.loads(reader.read())

            for elem in wordList:
                if counter == (len(wordList) - 1):
                    nextClause = ""
                else:
                    nextClause = " OR "                
                # read the next word
                word = elem.replace("'", "").replace(",", "").strip()
                query_words += "(" + word + ")" + nextClause
                counter += 1
            # end for loop
            # Add the generated string_query to the complete query                                
            
            query_obj["query"]["query_string"]["query"] = query_words

            # generate the json request
            with open(outputFile, "w") as writer:
                writer.write(json.dumps(query_obj, indent=4))
    finally:
        writer.close()
        reader.close()
# end of the script
