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
			"primaryLanguage":{"type":"text"}
		}
	}
}'