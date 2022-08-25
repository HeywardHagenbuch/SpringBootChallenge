# Important Project Notes
#### If using a mac keep in mind that in the README file the suggested curl commands are not setup to work on a Unix-based system, '\' are treated as escape characters.

### Current Curl Commands:
```bash
curl -F file=@input\OAK1.txt "http://localhost:8080/api/upload/OAK" -o output\OAK1.json
curl -F file=@input\OAK2.txt "http://localhost:8080/api/upload/OAK" -o output\OAK2.json
curl -F file=@input\PINE.txt "http://localhost:8080/api/upload/PINE" -o output\PINE.json
curl -F file=@input\MAPLE.txt "http://localhost:8080/api/upload/MAPLE" -o output\MAPLE.json

curl "http://localhost:8080/api/bundle?format=OAK" -o output\bundle1.json
curl "http://localhost:8080/api/bundle?format=OAK,PINE" -o output\bundle2.json
curl "http://localhost:8080/api/bundle?format=PINE,OAK" -o output\bundle3.json
curl "http://localhost:8080/api/bundle?format=OAK&format=PINE&format=MAPLE&minPrice=200&maxPrice=300" -o output\bundle4.json
```

### Updated Mac Curl Commands:
```bash
curl -F file=@input/OAK1.txt "http://localhost:8080/api/upload/OAK" -o output/OAK1.json
curl -F file=@input/OAK2.txt "http://localhost:8080/api/upload/OAK" -o output/OAK2.json
curl -F file=@input/PINE.txt "http://localhost:8080/api/upload/PINE" -o output/PINE.json
curl -F file=@input/MAPLE.txt "http://localhost:8080/api/upload/MAPLE" -o output/MAPLE.json

curl "http://localhost:8080/api/bundle?format=OAK" -o output/bundle1.json
curl "http://localhost:8080/api/bundle?format=OAK,PINE" -o output/bundle2.json
curl "http://localhost:8080/api/bundle?format=PINE,OAK" -o output/bundle3.json
curl "http://localhost:8080/api/bundle?format=OAK&format=PINE&format=MAPLE&minPrice=200&maxPrice=300" -o output/bundle4.json
```
