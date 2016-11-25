(ns showrum.data)

(defn main-header
  "Returns data for main header"
  [id title]
  {:db/id id
   :slide/order 1
   :slide/type :type/main-header
   :slide/title title})

(defn header
  "Returns data for header slide"
  [id order title]
  {:db/id id
   :slide/order order
   :slide/type :type/header
   :slide/title title})


(defn ask-me
  "Returns data for ask me anything slide"
  [id order]
  (header id order "Ask Me Anything"))

(defn fun-play
  "Returns data for fun and play"
  [id order]
  (header id order "Fun & Play"))

(defn lets-go
  "Returns data for let's go slide"
  [id order]
  (header id order "Let's go!"))

(def decks
  [{:db/id -1
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "21. - 25. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Intro"
    :deck/order 1
    :deck/slides [-2 -3 -4 -5 -6 -7 -8 -9 -10]}
   (main-header -2 "Contemporary Frontend Development")
   {:db/id -7
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "Who am I?"
    :slide/bullets ["@pepe | @damnpepe" "World Citizen" "Father" "Programmer"]
    :slide/notes "Last politics, we are all equal. Work on your family. Programming is adventure worth having. We are the makers of tomorrow and we should use our knowledge for good."}
   {:db/id -10
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "Who are you?"
    :slide/bullets ["Name & Origin" "Engagement & Experience" "Groups"]}
   {:db/id -3
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "Organization"
    :slide/bullets ["Simple made easy" "AM/PM" "Weekdays"]}
   {:db/id -4
    :slide/order 5
    :slide/type :type/bullets
    :slide/title "AM"
    :slide/bullets ["Clash of Titans" "Information" "Story"]
    :slide/notes "Two or more people, needed for evaluation. Titan of the day."}
   {:db/id -5
    :slide/order 6
    :slide/type :type/bullets
    :slide/title "PM"
    :slide/bullets ["Hands on" "Exercises" "Q&A"]
    :slide/notes "I would like to hear you,"}
   {:db/id -9
    :slide/order 7
    :slide/type :type/bullets
    :slide/title "Weekdays"
    :slide/bullets ["Monday - Tools"
                    "Tuesday - Structure"
                    "Wednesday - Styles"
                    "Thursday - Scripts"
                    "TGIF - Evaluation"]
    :slide/notes "All days have its meaning"}
   (ask-me -6 8)
   (lets-go -8 9)
   {:db/id -11
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "21. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Tools"
    :deck/order 2
    :deck/slides [-12 -13 -14 -15 -16 -17 -43 -44]}
   (main-header -12 "Frontend Development Tools")
   {:db/id -13
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "Chrome Devtools"
    :slide/bullets ["Standart" "Many extensions" "First task - install Canary"]}
   {:db/id -14
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "GitHub"
    :slide/bullets ["Git" "Your work" "OpenSource projects" "GitHub desktop" "Second task - register, folow, create"]}
   {:db/id -15
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "Atom Editor"
    :slide/bullets ["GitHub" "OpenSource project" "Many plugins" "Third task - install"]}
   {:db/id -43
    :slide/order 5
    :slide/type :type/bullets
    :slide/title "JSBin"
    :slide/bullets ["Fast check" "Found on the web" "JS libs, Doc/Style types"]}
   {:db/id -44
    :slide/order 6
    :slide/title "Today's aside - reason"
    :slide/type :type/bullets
    :slide/bullets ["Microservices" "Containers" "Serverless" ]}
   (ask-me -16 7)
   (fun-play -17 8)
   {:db/id -18
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "22. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Structure"
    :deck/order 3
    :deck/slides [-24 -19 -20 -21 -22 -23 -25 -26]}
   (main-header -19 "Frontend Development Structure")
   {:db/id -24
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "Document vs Data"
    :slide/bullets ["Web" "Apps" "Examples"]}
   {:db/id -20
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "HTML"
    :slide/bullets ["History" "Incarnations" "HTML5" "First task"]}
   {:db/id -21
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "XML"
    :slide/bullets ["History" "Not really human readable" "Incarnations"]}
   {:db/id -22
    :slide/order 5
    :slide/type :type/bullets
    :slide/title "JSON"
    :slide/bullets ["History" "Striving" "API" "Second task"]}
   {:db/id -23
    :slide/order 6
    :slide/type :type/bullets
    :slide/title "Others"
    :slide/bullets ["Markdown" "SVG" "Slim HAML Jade" "Binary"]}
   (ask-me -25 7)
   (fun-play -26 8)
   {:db/id -28
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "23. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Styles"
    :deck/order 4
    :deck/slides [-29 -32 -33 -34 -41 -42]}
   (main-header -29 "Frontend Development Styles")
   {:db/id -32
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "CSS"
    :slide/bullets ["History" "Basic structure" "Selectors" "Declarations & Properties"]}
   {:db/id -33
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "Preprocessor languages"
    :slide/bullets ["SASS" "Less" "Stylus" "Garden"]}
   {:db/id -34
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "Frameworks"
    :slide/bullets ["Bootstrap" "Semantic" "Tachyons" "Today's task"]}
   (ask-me -41 7)
   (fun-play -42 8)
   {:db/id -30
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "24. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Scripts"
    :deck/order 5
    :deck/slides [-31 -35 -36 -37 -38 -39 -40 -45]}
   (main-header -31 "Frontend Development Scripts")
   {:db/id -35
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "JavaScript"
    :slide/bullets ["History" "Proper name" "The web programming language" "ES8"]}
   {:db/id -36
    :slide/order 5
    :slide/type :type/bullets
    :slide/title "Transpilers"
    :slide/bullets ["Always JS on the end" "CofeeScript" "TypeScript" "Elm" "️❤️ ClojureScript ❤️"]}
   {:db/id -37
    :slide/order 6
    :slide/type :type/bullets
    :slide/title "Frameworks"
    :slide/bullets ["Like a lot" "jQuery" "React.js" "Angular.js" "Ember.js" "Google Closure"]}
   {:db/id -38
    :slide/order 7
    :slide/type :type/bullets
    :slide/title "Not only frontend"
    :slide/bullets ["Node.js" "npm" "Express.js"]}
   {:db/id -45
    :slide/order 8
    :slide/type :type/bullets
    :slide/title "Material Design"
    :slide/bullets ["Google" "Not finished" "Movie Time"]}
   (header -39 9 "+420 774 278 707")
   (fun-play -40 10)
   {:db/id -46
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "24. 11. 2016"
    :deck/place "CULS in Prague"
    :deck/title "Last ones"
    :deck/order 6
    :deck/slides [-47 -48 -49 -50 -51]}
   (main-header -47 "Contemporary Frontend World")
   (header -51 2 "https://www.surveymonkey.com/r/H2KCTVJ")
   (header -48 3 "Thank you all!")
   {:db/id -49
    :slide/order 4
    :slide/type :type/bullets
    :slide/title "More pepe to you"
    :slide/bullets ["@damnpepe Twitter/Mixcloud/Soundclound" "@pepe GitHub" "Quora"]}
   (header -50 5 "See ya on the back")])
