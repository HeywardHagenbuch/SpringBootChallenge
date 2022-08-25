# HLN Spring Boot Challenge

## Submission Instructions

* ✔ Provide your GitHub username, and we'll invite you to a project which contains the challenge.
* ⚠️Don't fork the challenge repository on GitHub or submit a pull request directly there, as that would make your solution public for
  others to see. Instead, just clone the repository and work locally on it.
* 📧 When you're done, squash your work into a single commit and use `git format-patch` to generate a patch file. Email the patch file
  to us as your challenge submission.

## HLN Challenge

Implement the following endpoints using spring boot inside this project.

### POST /api/upload/[type]

This should accept a file upload using multipart/form-data. The file is a non-standard TDF formatted file containing an id (assume a
positive long value) and a price with a dollar sign (assume that $XX.X is acceptable).

The data should be parsed and saved to the H2 database tables of `wood` and `wood_type` (using [type] in the path). The file may or
may not include a header (assume that the fields will be labeled “id” and “price”) and may include additional tabs. Duplicate
entries that are identical can be ignored, but uploads with bad data, no valid data, or are otherwise invalid, should return a 400
HTTP Response. Entries in the database can be overwritten. A successful upload will return 200 with all the current values of the
given type from the database (sorted by price descending, then id ascending) in the following JSON format:

```
[ 
    { 
        “id”: number, 
        “price”: number 
    } 
]
```

### GET /api/bundle?format=[format]&minPrice=[minPrice]&maxPrice=[maxPrice]

This should return “bundles” of wood with the given format. The [format] parameter indicates what wood types are included in the
bundle and the sorting. The format can be comma separated or given as multiple querystring parameters. The [minPrice] and [maxPrice]
parameters are not required, but will filter the bundles (based on bundle price) if set. A 400 HTTP Response should be returned
if the [format] has duplicates, contains a type that does not exist in the database, or is otherwise invalid.

Each bundle will be assigned an id that is a hyphen separated concatenation of the wood ids (using the order specified in [format]).
A successful response will return 200 HTTP Response with the bundles (sorted by bundle price descending, then by bundle id
ascending, with each bundle sorted by wood type using the order form [format]) in the following JSON format:

```
{ 
	“bundles”: [
		{ 
			“id”: string, 
			“bundle”: [
				{ 
					“type”: string, 
					“id”: number, 
					“price”: number
				}
			], 
			“price”: number
		}
	], 
	“total”: number
}
```

## Sample Data 

Sample data with expected output is provided. The output files were generated using the following curl commands:

```
curl -F file=@input\OAK1.txt "http://localhost:8080/api/upload/OAK" -o output\OAK1.json
curl -F file=@input\OAK2.txt "http://localhost:8080/api/upload/OAK" -o output\OAK2.json
curl -F file=@input\PINE.txt "http://localhost:8080/api/upload/PINE" -o output\PINE.json
curl -F file=@input\MAPLE.txt "http://localhost:8080/api/upload/MAPLE" -o output\MAPLE.json

curl "http://localhost:8080/api/bundle?format=OAK" -o output\bundle1.json
curl "http://localhost:8080/api/bundle?format=OAK,PINE" -o output\bundle2.json
curl "http://localhost:8080/api/bundle?format=PINE,OAK" -o output\bundle3.json
curl "http://localhost:8080/api/bundle?format=OAK&format=PINE&format=MAPLE&minPrice=200&maxPrice=300" -o output\bundle4.json
```

**Assume that the output will be read by another program. No additional libraries should be added to the pom.xml. This exercise will
be judged on the use of standard coding practices (including proper oo design, error handling, formatting, and parameter checking),
efficiency, scalability, accuracy, and attention to detail.**