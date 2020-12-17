import json
from pathlib import Path

inputFile = "../output/animals.json"
outputFile = "./queries/query1.json"
string_query = ""
counter = 0

query_obj = {
    "query": {
      "bool": {
        "must": [
          { "query_string": {
            "fields": ["title^2", "synopsis", "plotKeywords^3"], 
            "query": ""}
          },
          { "range": { "releaseYear": { "gte": 1950 }}
            
          }
        ]
      }
    }
  }


if (Path(outputFile).is_file()) == False:    
    # the query request has not been created yet. Create it        
    try:    
        with open(inputFile, "r") as reader:
            
            fileLines = reader.readlines()
            totalLines = len(fileLines)        
            
            for line in fileLines:
                #Exclude the first and the last line,(the brackets of the array)
                if ((counter != 0)) and (counter != totalLines-1):
                    if counter == (totalLines-2):
                        nextClause = ""
                    else:
                        nextClause = " OR "                
                    animalName = line.replace("\"", "").replace(",", "").strip()                
                    string_query += "(" + animalName + ")" + nextClause
                counter += 1
            # end for loop
            query_obj["query"]["bool"]["must"][0]["query_string"]["query"] = string_query
            with open(outputFile, "w") as writer:
                writer.write(json.dumps(query_obj, indent=4))                  

    finally:    
        writer.close()
        reader.close()
#end of the script
        
        
        