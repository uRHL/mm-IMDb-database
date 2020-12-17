query_string = {
  "query_string" : {
    "fields" : ["title", "synopsis"],
    "query" : ""
  }
}

query_obj = {
  "query" : {
      "bool" : {
      "should" : []
    }
  }
}
query_words = ""

print(249%250)
print(499%250)
print(749%250)
print(999%250)

for i in range(0, 1200):
    query_words += "a"
    if (i % 250) == 0:
        #Every 250 words a new query string is added to the bool query
        query_string["query_string"]["query"] = query_words
        query_obj["query"]["bool"]["should"].append(query_string)
        query_words = ""
if (len(query_words)>0):
    #Add the remaining words to the query
    query_string["query_string"]["query"] = query_words
    query_obj["query"]["bool"]["should"].append(query_string)
#print(query_obj)
