#Film Data Base
This is a NoSQL database for films. The films' informaiton is scrapped from the web portal IMDb using JSoup. The documents are inserted in an Elastic Search index, which provides 4 predefined queries about films.

#Obtaining the required data

##Attributes scrapped from the excel

- imdbID 
- filmURL 
- posterURL 
- title 
- avgScore 
- genres 

##Attributes scrapped from the filmURL

###releaseYear 	
	1) Query <a title="See more release dates">
	2) Extract the element's text
	- ie: 2 July 1999 (Spain)
	- ie: TV Series (2014-)
	
###mainActors 
	1) Query <a href~=[whatever]fullcredits[whatever] >
	2) Scrap that link to find <table class="cast_list">	
	3) Query all elements <a href~=/name[whatever]>
	4) Extract the element's text
	
###synopsis 
	1) Query <a href~=[whatever]plotsummary[whatever]>
	2) Scrap the link
	3) Query <li id="synopsis-[whatever]">
	4) Extract the element's text

IF text.contains("It looks like we don't have a Synopsis for this title yet") THEN

	5) Discard the previously extracted text
	6) Query the first <li id~=summary-[whatever]>
	7) Extract the element's text
	
###plotKeywords 
	1) Query <a href~=[whatever]keywords[whatever]>
	2) Follow the link
	3) Query all elements <a href~=/search/keyword[whatever]
	4) Extract the element's text
	
###filmmingLocations 	
	1) Query <a href~=[whatever]locations>
	2) Follow the link
	3) Query all elements <a href~=[whatever]locations[whatever]
	4) Extract the element's text
	
###originCountry
	1) Query <a href~=[whatever]country_of_origin=[whatever]
	2) Extract the element's text
	
###primaryLanguage 
	1) Query <a href~=[whatever]primary_language=[whatever]
	2) Extract the element's text

#Creating the mapping in Elastic Search
	curl -X PUT "localhost:9200/films?pretty" -H 'Content-Type: application/json' -d
	'{
		"mappings":{
			"properties":{
				"imdbID":{"type":"keyword"},
				"title":{"type":"text"},
				"releaseYear":{"type":"integer"},
				"avgScore":{"type":"double"},
				"genres":{"type":"text"},
				"mainActors":{"type":"text"},
				"synopsis":{"type":"text"},
				"plotKeywords":{"type":"text"},
				"filmingLocations":{"type":"text"},
				"originCountry":{"type":"text"},
				"primaryLanguages":{"type":"text"},
			}
		}
	}'

#Indexing the documents into Elastic Search

##Using Bulk API
	curl -XPOST -H "Content-Type: application/json" "localhost:9200/film_test/_bulk?pretty" --data-binary "@bulkTasks.json"

##Additional information
- TEST_URL = "https://www.imdb.com/title/tt0111161"
- RegEx created to find non-ascii chars: https://regex101.com/r/b1fbWM/1
