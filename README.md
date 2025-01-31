# Setup

First you need to set up vars to you vm to your project:
```shell
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
```
note: execute it in same dir as _**pom.xml**_ or set it hardcode.

Then set up variables to your CI/CD GitLab variables:
- $CI_REGISTRY_USER
- $CI_REGISTRY_NAME
- $CI_REGISTRY_PASSWORD

# Specification

```json
curl -X POST -H "Content-Type: application/json" \
     -d '{"login":"nik", "email":"nik@example.com", "password":"nik", "role":["user"]}' \
     http://cas-back-svc:8080/api/auth/signup
```

```json
curl -X POST -H "Content-Type: application/json" \
     -d '{"login":"nik","password":"nik"}' \
     http://cas-back-svc:8080/api/auth/signin
```

```json
Пример: 
api_base_url=http://cas-back-svc:8080

{
  "getCourses": {
    "url": "/course",
    "request": {
      "count": 0,
      // -1 для полного списка
      "offset": 0
    },
    "response": {
      "total": 0,
      "courses": [
        {
          "id": 0,
          "name": "",
          "description": "",
          "startDate": "01.01.1900",
          "endDate": "01.01.1900"
        }
      ]
    }
  },
  "getStudentCourses": {
    "url": "student/course/",
    "request": {
      "count": 0, // -1 для полного списка
      "offset": 0
    },
    "response": {
      "total": 0,
      "courses": [
        {
          "id": 0,
          "name": "",
          "description": "",
          "startDate": "01.01.1900",
          "endDate": "01.01.1900",
          "experience": 0, //опыт студента за курс. общий опыт студента вычислять наверное нет смысла, так как сумму легко посчитать в js сразу
          "progress": 0 //прогресс по курсу в процентах
        }
      ]
    }
  },

  "getCourse": {
    "url": "/course/{courseId}",
    "request": {
    },
    "response": {
      "course": {
        "id": 0,
        "name": "",
        "description": "",
        "startDate": "01.01.1900",
        "endDate": "01.01.1900",
        "chapters": [
          {
            "id": 0,
            "title": "",
            "orderNumber": 0,
            "children": [ ]
          }
        ]
      },
      "progress": 0,
      "experience": 0,
      "guildId": 0
    }
  },

  "getChapterContent": {
    "url": "/course/{courseId}/chapter/{chapterId}",
    "request": {
    },
    "response": [
      {
        "items": [
          {
            "id": 0,
            "orderNumber": 0,
            "typeItem": "INFO|QUESTION",
            "typeQuestion": "OPEN|SINGLE|PLURAL",
            "content": "", // тут должен быть полноценный редактор хтмл, в котором может быть все, что угодно (в том числе видео. видео скорее всего будет ссылкой на видео в ютубе(в цк например видео курсов выкладывали на ютуб и а опенеду просто ссылка на видос). Дл редактирования контента инфо преподавателем нужен редактор. для этого можно использовать библиотеку длч фронта ckeditor
            "variants": [
              {
                "content": ""
              }
            ],
            "tryNumber": 0,
            "passed": false
          }
        ]
      }
    ]
  },

  "updateProgress": { //GET, присылается, когда студент открывает инфо первый раз, при том что оно было непрочитано
    "url": "/student/info/{infoId}",
    "request": {
    },
    "response": {//200 в случае успеха, код ошибки, если иначе
      "passed": false,
      "tryNumber": 0,
      "experience": 0,
      "progress": 0
    }
  },
        
   "setAnswer": { //  POST
   "url": "/course/question",
    "request": {
      "questionId": 0,
      "variantIds": [1,2,3,4,5],
      "openAnswer": ""
    },
    "response": {
      "passed": false,
      "tryNumber": 0,
      "experience": 0,
      "progress": 0
    }
  },

  "enrollInACourse": { //POST
    "url": "/student/course",
    "request": {
      "courseId": 0
    },
    "response": { //курс на который подписался
      "id": 0,
      "name": "",
      "description": "",
      "startDate": "01.01.1900",
      "endDate": "01.01.1900",
      "experience": 0,
      "progress": 0
    }
  },

  "ping": { //GET, вызывать с определенным периодом например раз в 5 минут, обновляет время последнего онлайн
    "url": "/student/ping",
    "request": {
    },
    "response": {
    }
  },

  "createGuild": { //POST
    "url": "/guild/",
    "request": {
      "tagName": ""
    },
    "response": {
      //строка с сообщением об успехе
    }
  },

  "subscribeToGuild": { //PUT
    "url": "/guild/subscription/{id}",

    "response": {
      //строка с сообщением об успехе
    }
  },

  "unsubscribeGuild": { //POST
    "url": "/guild/unsubscribe/{id}", //id гильдии

    "response": {
      "deletedGuildId": -1 //поле равно айди гильдии в случае если отписался последний человек и гильдия была удалена
      "newLeader": -1 //поле равно айди юзера который стал лидером, если удалился чел который был лидером
    } //если подобных последствий не было то тела ответа не будет, чел просто отписался
  },

  "getGuildList": { //GET
    "url": "/guild/",
    "request": {
      "orderBy": "name"|"score"
    },

    "response": [{
      "id": 0,
      "tagName":  "",
      "leader": {
        "id": 0,
        "login": "",
        "email": "",
        "experience": 0,
        "lvl": 0,
        "roles": [],
        "lastOnline": 0, //таймстемп последнего онлайн
        "location": { //чаптер на который заходил последний(крайний) раз
          "id": 0,
          "name": ""
        }
      },
      "guildScore": 0,
      "lvl": 0,
      "users": []
    }]
  },
  "getGuild": { //GET
    "url": "/guild/{id}",

    "response": {
      "id": 0,
      "tagName":  "",
      "leader": {
        "id": 0,
        "login": "",
        "email": "",
        "experience": 0,
        "lvl": 0,
        "roles": [],
        "lastOnline": 0, //таймстемп последнего онлайн
        "location": { //чаптер на который заходил последний(крайний) раз
          "id": 0,
          "name": ""
        }
      },
      "guildScore": 0,
      "lvl": 0,
      "users": [],
      "description": ""
    }
  },

  "changeGuildDescription": { //PUT
    "url": "/guild/leader/edit", //описание подавать как Body

    "response": {
      "String": "Guild 'tagName' description changed by 'userLogin'."
    }

  },

  "getUserGuildIfHas": { //GET
    "url": "/student/ping/guildId", //описание подавать как Body

    "response": {
      "String": "guildId or String 'null'" //body
    }

  }

}

//в случае ошибки в респонз будет параметр error с сообщением об ошибке
```

