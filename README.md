# spoiler-alert (Scala)

This sample application is maintained at https://github.com/amitkmrjha/spoiler-alert.

#### Documentation
```
spoiler-alert application is rest-api microservices using lagom and akka peristence to achieve 
event sourcing & CQRS. Keeping in mind the the scalibility where million of user-series-progress events
are stored in cassandra journal table and then using read side processor to create view table.
These view table are designed and optimised according to query reqirement.This architecure also help use 
to easily model any new requirement by adding new view and replay all event.

Query 1 :  get all spoiler for given set of input user.
Event generated through CreateSpoilerAlert command is used to populate "spoileralerts_by_users_series"
table.This table has primary key as [user_name,series_name] for optimed query.
Give an set of input user Seq[String] , we contruct a cassandra CQL query to fetch all record for
these user on one go. There upon we use grouby & filter to populate the response.

Query 2 : get user who are same in progress.
Event generated through CreateSpoilerAlert command is used to populate 
"spoileralerts_by_series_percentage" table.This table has primary key [series_name,percentage] which helps
us to do very fast look up for all same progess user in series.

Over all spoiler-alert microserive approach is highly scalable as user-series persistent entity can live 
in multiple custer node using sharding and this will be highly resilient and fault-tolrent .

```

#### Getting started

To get started make sure you have sbt and git installed on your system.

#### Running: Prerequisites

- You will need to cassandra installed and running see application conf for detail:
- We need cassandra user created with valid pass word. please enter the detail in application conf before running the application.
- create and configure the key space in cassandra as per spoiler-alert-impl\src\main\resources\gen-schema.cql


#### Running

On another terminal, clone this repo and run the sample app using the command `sbt runAll`:

```
git clone https://github.com/amitkmrjha/spoiler-alert.git
cd spoiler-alert
sbt clean
sbt compile

sbt runAll

```
- once the application is started endpoints are available on http://localhost:9000/

#### Testing The endpoint

###### Create new user-series-progress
```
Request
curl --location --request POST 'http://localhost:9000/api/vi/userseries' \
--header 'Content-Type: application/json' \
--data-raw '{
	"userName":"Amit",
	"seriesName":"suit",
	"percentage":80
}'

Response
{
    "id": "c684d250-b478-11ea-8fcc-858e2d0736fe",
    "key": "SAM-OZARK",
    "userName": "Sam",
    "seriesName": "ozark",
    "percentage": 80
}
```

###### Query spoiler-alert given give set of users

```
Request
curl --location --request POST 'http://localhost:9000/api/vi/userseries/spoilers' \
--header 'Content-Type: application/json' \
--data-raw '[
    "Amit",
    "Amit1",
    "Amit2",
    "Amit3",
    "Amit4",
    "Amit5",
    "Amit6"
]'

Response

[
    {
        "userName": "Amit",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit1",
                    "Amit2",
                    "Amit3",
                    "Amit4",
                    "Amit5",
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit4"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": [
                    "Amit2",
                    "Amit3",
                    "Amit4",
                    "Amit6"
                ]
            }
        ]
    },
    {
        "userName": "Amit1",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit2",
                    "Amit3",
                    "Amit5",
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit",
                    "Amit2",
                    "Amit3",
                    "Amit4",
                    "Amit5"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": [
                    "Amit",
                    "Amit2",
                    "Amit3",
                    "Amit4",
                    "Amit6"
                ]
            }
        ]
    },
    {
        "userName": "Amit2",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit3",
                    "Amit5",
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit",
                    "Amit3",
                    "Amit4"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": []
            }
        ]
    },
    {
        "userName": "Amit3",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit5",
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit",
                    "Amit4"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": [
                    "Amit2",
                    "Amit4",
                    "Amit6"
                ]
            }
        ]
    },
    {
        "userName": "Amit4",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit2",
                    "Amit3",
                    "Amit5",
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": []
            },
            {
                "seriesName": "dark",
                "spoilers": [
                    "Amit2",
                    "Amit6"
                ]
            }
        ]
    },
    {
        "userName": "Amit5",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": [
                    "Amit6"
                ]
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit",
                    "Amit3",
                    "Amit4"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": []
            }
        ]
    },
    {
        "userName": "Amit6",
        "seriesSpoiler": [
            {
                "seriesName": "GOT",
                "spoilers": []
            },
            {
                "seriesName": "madmen",
                "spoilers": []
            },
            {
                "seriesName": "suit",
                "spoilers": [
                    "Amit",
                    "Amit1",
                    "Amit2",
                    "Amit3",
                    "Amit4",
                    "Amit5"
                ]
            },
            {
                "seriesName": "dark",
                "spoilers": []
            }
        ]
    }
]

```

###### Query users having same progress in for a given  user & series

```
Request
curl --location --request GET 'http://localhost:9000/api/vi/userseries/match/Amit/GOT'

Response
[
    "Amit1",
    "Amit2",
    "Amit3",
    "Amit4"
]

```





