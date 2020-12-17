#!/bin/bash

prompt="\nSelect the number of the query you want to execute"
results_prompt="These are the results"
exit_program="0. Exit the program"

# 1st query parameters
first_opt="1. Films about animals produced since 1950"
# Build the first query if it does not exist
python3 buildQuery1.py
first_query=$(cat ./queries/query1.json)

# 2nd query parameters
second_opt="2. Actors with the highest number of ‘adventure’ films"
second_query=$(cat ./queries/query2.json)

# 3rd query parameters
third_opt="3. Films about ‘crimes’available in the collection && number of these films per year"
third_query=$(cat ./queries/query3.json)

# 4th query parameters
fourth_opt="4. Films with social contents in Spain and LatinAmerica."
# Build the first query if it does not exist
python3 buildQuery4.py
fourth_query=$(cat ./queries/query4.json)
#
execute_query=""
test=$(cat ./queries/query1.json)

#Defaul value
selected_opt=9
while [ $selected_opt -gt 0 ]; do
    echo -e "$prompt\n\n$first_opt\n$second_opt\n$third_opt\n$fourth_opt\n\n$exit_program"
    read selected_opt
    case $selected_opt in
    0)
        echo -e "\nYou selected $exit_program\nProgram completed"
        ;;
    1)
        echo -e "\nYou selected $first_opt\n$results_prompt\n"
        echo "$animal_names"
        curl -X GET "localhost:9200/films/_search?size=10&&pretty" -H 'Content-Type: application/json' -d "$first_query"
        ;;
    2)
        echo -e "\nYou selected $second_opt\n$results_prompt\n"
        curl -X GET "localhost:9200/films/_search?size=10&&pretty" -H 'Content-Type: application/json' -d "$second_query"
        ;;        
    3)
        echo -e "\nYou selected $third_opt\n$results_prompt\n"
        curl -X GET "localhost:9200/films/_search?size=10&&pretty" -H 'Content-Type: application/json' -d "$third_query"
        ;;
    4)
        echo -e "\nYou selected $fourth_opt\n$results_prompt\n"
        curl -X GET "localhost:9200/films/_search?size=10&&pretty" -H 'Content-Type: application/json' -d "$fourth_query"
        echo "$social_contents"
        ;;
    *)
        #non recognized option. Restore the default value
        echo -e "\nOption $selected_opt not recognized\n"
        selected_opt=9
        ;;
    esac            
done
