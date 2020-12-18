# Film Data Base

This is a NoSQL database for films. The films' information is scrapped from the web portal IMDb using JSoup. The documents are [inserted](#adding-data-to-elastic-search) in our Elastic Search node. [4 predefined queries](#elastic-search-queries) are included to interact with all the stored information.

By [@uRHL](https://github.com/uRHL)

# Obtaining the required data

## Attributes scrapped from the Excel

- imdbID 
- filmURL 
- posterURL 
- title 
- avgScore 
- genres 

## Attributes scrapped from the filmURL

### releaseYear
This datum is useful to execute queries related with time. For example, ordering the collection from older to newer, or getting the most popular films of the last 10 years.

Scrapping pseudocode:

    1. Query <a title="See more release dates">
    2. Extract the element's text
    	- ie: 2 July 1999 (Spain)
    	- ie: TV Series (2014-)
  	3. Parse the release year from the extracted text
	
### mainActors
This datum is essential to execute queries involving actor names. For example, get the films where Morgan Freeman appears, or [which actors have worked in more adventure films](#2-actors-with-the-highest-number-of-adventure-films).

Scrapping pseudocode:

    1. Query <a href~=[whatever]fullcredits[whatever] >
    2. Scrap that link to find <table class="cast_list">	
    3. Query all elements <a href~=/name[whatever]>
    4. Extract the element's text
	
### synopsis 
The synopsis, or summary of the film, is useful to search keywords, names, locations... that appear in the film. Moreover *match* queries can be used to check if, given a word set, there is any ocurrence of those words within the synopsis. For example, to [check if some kind animal is mentioned in the film](#1-films-about-animals-produced-since-1950).

Scrapping pseudocode:

    1. Query <a href~=[whatever]plotsummary[whatever]>
    2. Scrap the link
    3. Query <li id="synopsis-[whatever]">
    4. Extract the element's text

	IF text.contains("It looks like we don't have a Synopsis for this title yet"). THEN

    5. Discard the previously extracted text
    6. Query the first <li id~=summary-[whatever]>
    7. Extract the element's text
	
### plotKeywords 
With this information we expect to be able to query films related with an specific term. For example, similarly to the synopsis, we can query films that are related with war or violence, even though it is not explicitly mentioned in the attribute *genres*. 

Scrapping pseudocode:

    1. Query <a href~=[whatever]keywords[whatever]>
    2. Follow the link
    3. Query all elements <a href~=/search/keyword[whatever]
    4. Extract the element's text
	
### filmingLocations
This datum intented to support the [4th query](#4-films-with-social-contents-in-spain-and-latinamerica). Nevertheless, while going through the scrapping process we realized that most of the films do not have this information available in their respective web pages. In the end we decided to keep this attribute to enhance queries about specific social contents appearing in a film, such as were it has been filmed.

Scrapping pseudocode:

    1. Query <a href~=[whatever]locations>
    2. Follow the link
    3. Query all elements <a href~=[whatever]locations[whatever]
    4. Extract the element's text
	
### originCountry
This datum accomplish a similar mission to the [filming locations](#filminglocations). For example, it can be used to query the films that come from an specific country, or that have some kind of relation with it.

Scrapping pseudocode:

	1. Query <a href~=[whatever]country_of_origin=[whatever]
	2. Extract the element's text
	
### primaryLanguages 
Similar to [origin country](#origincountry) and [filming locations](#filminglocations), this datum provides information about the origin of the film. That information could be useful to search for social contents, as the [4th query](#4-films-with-social-contents-in-spain-and-latinamerica) intends.

Scrapping pseudocode:

	1. Query <a href~=[whatever]primary_language=[whatever]
	2. Extract the element's text

## Scrapping process results

The statistical data generated through all the scrapping process can be found in the file ***scrappingApp.log***. It contains information such as the average scrapping time per film, the error causing that a film could not be scrapped and more. We should mention that this process was splitted in 5 smaller tasks of 9000 films each. The reason is that IMDb servers will reject any request after scrapping ~10000 films in a round. It makes sense, since they do not want us to make a DoS attack to  In the end, the total time inverted scrapping the web was around 10 hours.

# Adding data into Elastic Search

## Create the mapping 

Once the all the data is gathered, the index structure need to be properly defined so that we can get maxiumum profit from searches.

Three attributes are marked as *keywords*: the imdbID (unique identifier), mainActors (to perform queries about specific actor's names, like the [query 2](#2-actors-with-the-highest-number-of-adventure-films)) and plotKeywords (to perform queries about specific contents appearing in a film, like [query 1](#1-films-about-animals-produced-since-1950)).

The rest of the attributes are text, or numeric values. Not all the attributes are of type 'text' because that type does not support some of the query functionalities provided by the Elastic Search API. For example, 'text' attributes cannot be aggregated.

The script ***mapping.sh*** creates the mapping in our ElasticSearch node.

	curl -X PUT "localhost:9200/films?pretty" -H 'Content-Type: application/json' -d
	'{
		"mappings": {
			"properties": {
				"imdbID": {
					"type": "keyword"
				},
				"title": {
					"type": "text"
				},
				"releaseYear": {
					"type": "integer"
				},
				"avgScore": {
					"type": "double"
				},
				"genres": {
					"type":"text"
				},
				"mainActors": {
					"type": "keyword"
				},
				"synopsis": {
					"type": "text"
				},
				"plotKeywords": {
					"type":"keyword"
				},
				"filmingLocations": {
					"type": "text"
				},
				"originCountry": {
					"type": "text"
				},
				"primaryLanguage": {
					"type": "text"
				}
			}
		}
	}'

## Indexing the documents

### Bulk API

The script ***dump_data.sh*** indexes all the films in our ElasticSearch node. It requires the file ***bulkTasks.json*** generated previously. Nevertheless, this file is to big to send it in a single *curl* request. Therefore, it is splitted in smaller files (~3 MB). Each of them is sent to the Bulk API so that it can execute the tasks contained in our request.
The source code of the script can be found in ***dump_data.sh***. The next command is an example of how to use the Bulk API with the command *curl*

	curl -XPOST -H "Content-Type: application/x-ndjson" "localhost:9200/films/_bulk?pretty" --data-binary "@bulkTasks.json"

Is important to notice that Bulk API expects a [ndjson](http://ndjson.org/) file. This is json file in which every line is delimited by a new line character (\n). This is an example of one of the bulk tasks created, with the proper *ndjson* format. 
	
	{"index":{}}
	{"imdbID":10040,"title":"Daddy-Long-Legs","synopsis":"An orphan discovers that she has an anonymous benefactor who is willing to pay her college tuition, unaware he\u0027s the same man who has been romantically pursuing her.","originCountry":"USA","releaseYear":1919,"avgScore":7.6,"genres":["Comedy","Drama"],"mainActors":["Mary Pickford","Milla Davenport","Percy Haswell","Fay Lemport","Mahlon Hamilton","Lillian Langdon","Betty Bouton","Audrey Chapman","Marshall Neilan","Carrie Clark Ward","Wesley Barry","True Boardman","Jeanne Carpenter","Estelle Evans","Fred Huntley","Frankie Lee","Joan Marsh"],"plotKeywords":["benefactor","boarding school","orphan","protective male","girl","rivalry","novelist","practical joke","massachusetts","trustee","marriage proposal","snobbery","jealousy","may december romance","orphanage","college student","class distinction","millionaire","based on play","based on novel"],"filmingLocations":["Crags Road, Malibu Creek, California, USA","Busch Gardens - S. Grove Avenue, Pasadena, California, USA"],"primaryLanguages":["None","English"]}


# Elastic Search Queries
The source created to implement the queries can be found code the folder /scripts/queries

### 1. Films about animals produced since 1950

This query is of type *bool*. That means that it allow us to use boolean operators within the different search parameters. In this case, and AND condition is stablished between two statements: first, matches with any word included in our animal-dictionary, and second, the film's release year must be after 1950. The dictionary is to big to show it here, the complete request can be found in the file *query1.json*. The structure of the query is as follows:

	{
		"query": {
			"bool": {
			"must": [
				{ "query_string": {
				"fields": ["title^2", "synopsis", "plotKeywords^3"], 
				"query": animal-dictionary }
				},
				{ "range": { "releaseYear": { "gte": 1950 }}
				
				}
			]
			}
		}
	}

### 2. Actors with the highest number of ‘adventure’ films

For this query it was essential to set the type of *mainActors* attribute to *keyword*. Otherwise it would not be possible to make an aggregation on that attribute. The query is divided in two nested aggregations. First, we select all the films related with 'Adventure'. Then, we group all those films by the main actors, so that we can count who has appeared more times in this types of films. The query structure is the following:

	{
		"size": 0, 
		"aggs": {
			"adventure_agg": {
				"filter": {
					"query_string": {
						"fields": ["genres^2", "plotKeywords"], 
						"query": "(adventure) OR (adventures)"
					}
				}, 
				"aggs": {
					"top_20_adventure_actors": {
						"terms": {
							"field": "mainActors",
							"size": 20
						}
					}
				}
			}
		}
	}

The effectiveness of the query can be checked with another search, included in the file ***testQuery2.json***. If we run it, we can see that effectively, a man called "Frank Welker" is the actor that appears more times in adventures films (118 films!).

### 3. Films about ‘crimes’ available in the collection && number of these films per year

The query three retrives the list (only the first 10 are shown) of crime fils stored in our Elastic Search node. Additionally, it also shows the number of crime films per year, from 2020 to 1920.

	{
		"size": 10, 
		"query": {
			"query_string": {
				"fields": ["genres^2", "plotKeywords"], 
				"query": "(crime) OR (crimes)"
			}
		},
		"aggs": {
			"Terms_Aggregation" : {
				"terms": { 
					"field": "releaseYear",
					"size": 150,
					"order": {
						"_key": "desc"
					}
				}
			}
		}
	}

### 4. Films with social contents in Spain and LatinAmerica

First, a dictionary of spanish words was generated from 2 different Wikipedia pages: [History of Spain](https://es.wikipedia.org/wiki/Historia_de_Espa%C3%B1a) and [History of South America](https://es.wikipedia.org/wiki/Historia_de_Sudam%C3%A9rica)
The dictionary is named ***spanish-word-dic.json***, anc contains 1024 words. Although more words could have been added, the size is fixed to accomplish the Elastic Search setting.

Some attributes are boosted against others. For example, we give higher punctuation to the matches with *plotkeywords*, while matches with synopsis are the less valuable.

The spanish-word-dictionary is to big to include it here, but the query structure is as follows:

	{
		"size": 10,
		"query": {
			"query_string": {
				"fields": [
					"title^2",
					"synopsis",
					"plotKeywords^4",
					"filmingLocations^2",
					"originCountry^2",
					"primaryLanguages^2"
				],
				"query": spanish-word-dic
			}
		}
	}

# How to run the project on your own computer

Note: If you want to go directly into Elastic Search, you can skip the Scrap stage and use the files I included in the directory /output. For that you will need to have [git-lfs](https://git-lfs.github.com/) installed in your computer. Otherwise, the file bulkTasks.json will be downloaded as a pointer, without data, since the file is bigger than the maximum GitHub file size (100MB).

### Scrap all the data.
1. Configure the ScriptingApp paramters. Those can be found in the class ScrappingApp.
   1. I recomend you to scrap the films in bunchs of 9000 films at most to avoid being banned by IMDb servers. The block is not permanent, do not worry, but if you try to do it all at once, you will find that most of the scraps have fail.
2. Compile the project and run the main class ScrappingApp.
    1. Each time you execute ScrappingApp, you will have a detailed log available to check how successful (or not) the process was. Look for the file ***scrappingApp.log***.
3. Once you have scrapped the 40000 films contained in the original Excel file you are good to continue to the next step.



### Prepare the Elastic Search node
1. I am assuming that you have Elastic Search installed in your machine or deployed with Docker. If that is not the case, you can find how to install it [here](https://www.digitalocean.com/community/tutorials/how-to-install-elasticsearch-logstash-and-kibana-elastic-stack-on-ubuntu-18-04).
2. Create the mapping. To do so, open a terminal and run the script ***mapping.sh***.
   
		bash mapping.sh

3. Index all the data you have gathered. All this data is stored in the ***file bulkTasks.json***. The file is so big that it cannot be sent in a single request. But do not worry, I have automated the process. Just execute the script ***dump_data.sh*** from the terminal and in 1 minute you will have 40K documents in your Elastic Search node.
   
		bash dump_data.sh

### Execute the queries
1. In a terminal, run the script ***elastic_queries.sh*** to test the 5 predefined queries that I included.

	bash elastic_queries.sh	

2. You can add your own queries by creating the required request file (*queryX.json*) and including a new option in the script *elastic_queries.sh*.
3. [*Knowledge will make you free*.](https://www.goodreads.com/quotes/9390784-knowledge-will-make-you-be-free)  Have fun!


# Conclusions

Through this project I have learn a lot. Not only about NLP or Elastic Search, but how to work with a complete framework that integrates many different funnctionalities. In the future I would like to implement a similar project for my own. I can say that I am very proud of the results, and all the effort invested.

