# Film Data Base
This is a NoSQL database for films. The films' information is scrapped from the web portal IMDb using JSoup. The documents are [inserted](#adding-data-to-elastic-search) in our Elastic Search node. [4 predefined queries](#elastic-search-queries) are created to interact with all the stored information.

# Obtaining the required data

## (basic) Attributes scrapped from the Excel

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
With this information we expect to be able to query films related with an specific term. For example, similarly to the synopsis, we can query films that are related with war or violence, even though that genre is not explicitly mentioned in the attribute *genres*. 

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

# Adding data into Elastic Search
## Create the mapping 
The script ***mapping.sh*** creates the mapping in our ElasticSearch node

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
				"primaryLanguages":{"type":"text"}
			}
		}
	}'

## Indexing the documents

### Bulk API
The script ***dump_data.sh*** indexes all the films in our ElasticSearch node. It requires the file ***bulkTasks.json*** generated previously. Nevertheless, this file is to big to send it in a single *curl* request. Therefore, it is splitted in smaller files (~3 MB). Each of them is sent to the Bulk API so that it can execute the tasks contained in our request.
The source code of the script can be found in ***dump_data.sh***. The next command is an example of how to use the Bulk API with the command *curl*

	curl -XPOST -H "Content-Type: application/json" "localhost:9200/film/_bulk?pretty" --data-binary "@bulkTasks.json"

# Elastic Search Queries
### 1. Films about animals produced since 1950

	GET /films/_search?size=10
	{
	"query": {
		"bool": {
		"must": [
			{ "query_string": {
			"fields": ["title²", "synopsis", "plotKeywords³"], 
			"query": "(Aardvark) OR (Albatross) OR (Alligator) OR (Alpaca) OR (Ant) OR (Anteater) OR (Antelope) OR (Ape) OR (Armadillo) OR (Donkey) OR (Baboon) OR (Badger) OR (Barracuda) OR (Bat) OR (Bear) OR (Beaver) OR (Bee) OR (Bison) OR (Boar) OR (Buffalo) OR (Butterfly) OR (Camel) OR (Capybara) OR (Caribou) OR (Cassowary) OR (Cat) OR (Caterpillar) OR (Cattle) OR (Chamois) OR (Cheetah) OR (Chicken) OR (Chimpanzee) OR (Chinchilla) OR (Chough) OR (Clam) OR (Cobra) OR (Cockroach) OR (Cod) OR (Cormorant) OR (Coyote) OR (Crab) OR (Crane) OR (Crocodile) OR (Crow) OR (Curlew) OR (Deer) OR (Dinosaur) OR (Dog) OR (Dogfish) OR (Dolphin) OR (Dotterel) OR (Dove) OR (Dragonfly) OR (Duck) OR (Dugong) OR (Dunlin) OR (Eagle) OR (Echidna) OR (Eel) OR (Eland) OR (Elephant) OR (Elk) OR (Emu) OR (Falcon) OR (Ferret) OR (Finch) OR (Fish) OR (Flamingo) OR (Fly) OR (Fox) OR (Frog) OR (Gaur) OR (Gazelle) OR (Gerbil) OR (Giraffe) OR (Gnat) OR (Gnu) OR (Goat) OR (Goldfinch) OR (Goldfish) OR (Goose) OR (Gorilla) OR (Goshawk) OR (Grasshopper) OR (Grouse) OR (Guanaco) OR (Gull) OR (Hamster) OR (Hare) OR (Hawk) OR (Hedgehog) OR (Heron) OR (Herring) OR (Hippopotamus) OR (Hornet) OR (Horse) OR (Human) OR (Hummingbird) OR (Hyena) OR (Ibex) OR (Ibis) OR (Jackal) OR (Jaguar) OR (Jay) OR (Jellyfish) OR (Kangaroo) OR (Kingfisher) OR (Koala) OR (Kookabura) OR (Kouprey) OR (Kudu) OR (Lapwing) OR (Lark) OR (Lemur) OR (Leopard) OR (Lion) OR (Llama) OR (Lobster) OR (Locust) OR (Loris) OR (Louse) OR (Lyrebird) OR (Magpie) OR (Mallard) OR (Manatee) OR (Mandrill) OR (Mantis) OR (Marten) OR (Meerkat) OR (Mink) OR (Mole) OR (Mongoose) OR (Monkey) OR (Moose) OR (Mosquito) OR (Mouse) OR (Mule) OR (Narwhal) OR (Newt) OR (Nightingale) OR (Octopus) OR (Okapi) OR (Opossum) OR (Oryx) OR (Ostrich) OR (Otter) OR (Owl) OR (Oyster) OR (Panther) OR (Parrot) OR (Partridge) OR (Peafowl) OR (Pelican) OR (Penguin) OR (Pheasant) OR (Pig) OR (Pigeon) OR (Pony) OR (Porcupine) OR (Porpoise) OR (Quail) OR (Quelea) OR (Quetzal) OR (Rabbit) OR (Raccoon) OR (Rail) OR (Ram) OR (Rat) OR (Raven) OR (Red deer) OR (Red panda) OR (Reindeer) OR (Rhinoceros) OR (Rook) OR (Salamander) OR (Salmon) OR (Sand Dollar) OR (Sandpiper) OR (Sardine) OR (Scorpion) OR (Seahorse) OR (Seal) OR (Shark) OR (Sheep) OR (Shrew) OR (Skunk) OR (Snail) OR (Snake) OR (Sparrow) OR (Spider) OR (Spoonbill) OR (Squid) OR (Squirrel) OR (Starling) OR (Stingray) OR (Stinkbug) OR (Stork) OR (Swallow) OR (Swan) OR (Tapir) OR (Tarsier) OR (Termite) OR (Tiger) OR (Toad) OR (Trout) OR (Turkey) OR (Turtle) OR (Viper) OR (Vulture) OR (Wallaby) OR (Walrus) OR (Wasp) OR (Weasel) OR (Whale) OR (Wildcat) OR (Wolf) OR (Wolverine) OR (Wombat) OR (Woodcock) OR (Woodpecker) OR (Worm) OR (Wren) OR (Yak) OR (Zebra)"}
			},
			{ "range": { "releaseYear": { "gte": 1950 }}
			
			}
		]
		}
	}
	}

### 2. Actors with the highest number of ‘adventure’ films

	GET _search
	{
	"query": {
		"bool": {
		"must": [
			{
			"term": {
				"genres": {
				"value": "adventure"
				}
			}
			}
		]
		}
	}
	}
### 3. Films about ‘crimes’available in the collection && number of these films per year
### 4. Films with social contents in Spain and LatinAmerica
First, a dictionary of spanish words was generated from 3 different Wikipedia pages: [1000 basic Spanish words](https://es.wiktionary.org/wiki/Ap%C3%A9ndice:1000_palabras_b%C3%A1sicas_en_espa%C3%B1ol), [History of Spain](https://es.wikipedia.org/wiki/Historia_de_Espa%C3%B1a), [History of South America](https://es.wikipedia.org/wiki/Historia_de_Sudam%C3%A9rica)
The dictionary is named "spanish-word-dic.json".  


## Additional information

