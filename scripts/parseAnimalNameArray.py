import json
fileName = "../output/animals.json"
string_query = ""
counter = 0
try:
    with open(fileName, "r") as reader:
        
        fileLines = reader.readlines()
        totalLines = len(fileLines)

        #dic = json.loads(reader.read())
        #print(dic)
        
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
finally:    
    print(string_query)
    reader.close()