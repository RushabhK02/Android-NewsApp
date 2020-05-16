const express = require('express')
const server = express();
const api_helper = require('./API_helper')
const cors = require('cors');
const googleTrends = require('google-trends-api');

// TODO: Do data processing here before sending
server.use(cors())

//setting the port.
server.set('port', process.env.PORT || 5000);
var guardianKey = "a35404af-183b-443d-b839-92b6505bed04";
var NYtimesKey = "0WTyqQilAQjEAjeFI9rtZ22Z1YDNRup3";

//Adding routes
server.get('/', (request, response) => {
    response.sendFile(__dirname + '/index.html');
});

server.get('/world', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    api_helper.make_API_call("https://content.guardianapis.com/world?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    if (art.blocks && art.blocks.body) {
                        description = art.blocks.body[0].bodyTextSummary;
                    }
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
                // console.log(formattedArticles);
            })
            .catch(err => {
                response.send(err);
            })
});

server.get('/politics', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        api_helper.make_API_call("https://content.guardianapis.com/politics?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    if (art.blocks && art.blocks.body) {
                        description = art.blocks.body[0].bodyTextSummary;
                    }
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
            })
            .catch(err => {
                response.send(err);
            })
});

server.get('/business', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        api_helper.make_API_call("https://content.guardianapis.com/business?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    if (art.blocks && art.blocks.body) {
                        description = art.blocks.body[0].bodyTextSummary;
                    }
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
            })
            .catch(err => {
                response.send(err);
            })
});

server.get('/technology', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        api_helper.make_API_call("https://content.guardianapis.com/technology?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    if (art.blocks && art.blocks.body) {
                        description = art.blocks.body[0].bodyTextSummary;
                    }
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
            })
            .catch(err => {
                response.send(err);
            })
});

server.get('/sports', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        api_helper.make_API_call("https://content.guardianapis.com/sport?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    if (art.blocks && art.blocks.body) {
                        description = art.blocks.body[0].bodyTextSummary;
                    }
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
            })
            .catch(err => {
                response.send(err);
            })
});


// For detailed article access
server.get('/article', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    let id = request.query.id;
        api_helper.make_API_call("https://content.guardianapis.com/" + id + "?api-key=" + guardianKey + "&show-blocks=all")
            .then(res => {
                // console.log(res);
                let art = res.response.content;
                let status = res.response.status;
                let formattedArticle = guardianDetailedArticle(art, newsChannel);
                // console.log(formattedArticle);
                response.send({ formattedArticle, status });
            })
            .catch(error => {
                response.send(error);
            })
});

guardianDetailedArticle = (art, newsChannel) => {
    try{
    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
    let title = art.webTitle;
    let artId = art.id;
    let pubDate = art.webPublicationDate;
    let description = '';
    if (art.blocks && art.blocks.body) {
      art.blocks.body.forEach(element => {
            description += element.bodyHtml;
        });
    }
    let artUrl = art.webUrl;
    if (checkIfImageExists(art, newsChannel)) {
        let assets = art.blocks.main.elements[0].assets;
        let imageUrl = assets[assets.length - 1].type == "image" ?
            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
            : 'default';
        return {
            artId, section, title, pubDate, description, artUrl, imageUrl, type: 'Guardian'
        }
    } else return {
        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
    }
    }catch(err ){
        console.log(err);
    }
}

server.get('/search', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    let kw = request.query.keyword;
        api_helper.make_API_call("https://content.guardianapis.com/search?q=" + kw + "&api-key=" + guardianKey + "&show-blocks=all")
            .then((res) => {
                let articles = res.response.results;
                // console.log(articles);
                let status = res.response.status;

                let formattedArticles = articles.map(art => {
                    let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                    let artId = art.id;
                    let title = art.webTitle;
                    let pubDate = art.webPublicationDate;
                    let description = '';
                    let artUrl = art.webUrl;
                    if (checkIfImageExists(art, newsChannel)) {
                        let assets = art.blocks.main.elements[0].assets;
                        let imageUrl = assets[assets.length - 1].type == "image" ?
                            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                            : 'default';
                        return {
                            artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                        }

                    } else return {
                        artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                    }

                })
                // .slice(0, 10);
                response.send({ formattedArticles, status });
            })
            .catch(error => {
                response.send(error);
            })
});

checkIfImageExists = (art, newsChannel) => {
    if (newsChannel == "NY") return true;
    try {
        let assets = art.blocks.main.elements[0].assets;
        if (assets == undefined || assets == null) return false;
        let imageUrl = assets[assets.length - 1].type == "image" ?
            (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
            : 'default';
        if (imageUrl == undefined || imageUrl == null) return false;
        return true;
    }
    catch (err) {
        return false;
    }
}

server.get('/trends', (request, response) => {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    let kw = request.query.keyword;
    googleTrends.interestOverTime({ keyword: kw, startTime: new Date("2019-06-01") })
        .then((res) => JSON.parse(res))
        .then((results) => {
            let values = results.default.timelineData.map((row) => row.value[0]);
            // console.log(values);
            response.send({ 'trend': values });
        })
        .catch(function (err) {
            console.error(err);
        });

});

server.get('/science', (request, response) => {
    let newsChannel = request.header('news-provider');
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    api_helper.make_API_call("https://content.guardianapis.com/science?api-key=" + guardianKey + "&show-blocks=all")
        .then(res => {
            let articles = res.response.results;
            // console.log(articles);
            let status = res.response.status;

            let formattedArticles = articles.map(art => {
                let section = art.sectionName.length > 0 ? art.sectionName : 'other';
                let artId = art.id;
                let title = art.webTitle;
                let pubDate = art.webPublicationDate;
                let description = '';
                if (art.blocks && art.blocks.body) {
                    description = art.blocks.body[0].bodyTextSummary;
                }
                let artUrl = art.webUrl;
                if (checkIfImageExists(art, newsChannel)) {
                    let assets = art.blocks.main.elements[0].assets;
                    let imageUrl = assets[assets.length - 1].type == "image" ?
                        (assets[assets.length - 1].file ? assets[assets.length - 1].file : 'default')
                        : 'default';
                    return {
                        artId, section, title, pubDate, description, artUrl, type: 'Guardian', imageUrl
                    }

                } else return {
                    artId, section, title, pubDate, description, artUrl, imageUrl: 'default', type: 'Guardian'
                }

            })
            // .slice(0, 10);
            response.send({ formattedArticles, status });
            // console.log(formattedArticles);
        })
        .catch(err => {
            response.send(err);
        })

});

server.get('/apphome', (request, response) => {
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    api_helper.make_API_call("https://content.guardianapis.com/search?order-by=newest&show-fields=starRating,headline,thumbnail,short-url&api-key=" + guardianKey)
    // + "&show-blocks=all")
        .then(res => {
            let articles = res.response.results;
            // // console.log(articles);
            let status = res.response.status;

            let formattedArticles = articles.map(art => {
                let section = art.sectionName.length > 0 ? art.sectionName : 'other';

                let artId = art.id;
                let title = art.webTitle;
                let pubDate = art.webPublicationDate;
                let artUrl = art.webUrl;
                let description = "";
                let imageUrl = art.fields.thumbnail;
                imageUrl = (imageUrl!=null && imageUrl!="")?imageUrl:'default';                
                return {
                    artId, section, title, pubDate, description, artUrl, imageUrl, type: 'Guardian'
                }

            }).slice(0, 10);
            // console.log(formattedArticles);
            response.send({ formattedArticles, status });
            // console.log(res);
            // response.send(res);
        })
        .catch(err => {
            response.send(err);
        })

});

//Express error handling middleware
server.use((request, response) => {
    response.type('text/plain');
    response.status(505);
    response.send('Error page');
});

//Binding to localhost://3000
server.listen(8080, () => {
    console.log('Express server started at port 8080');
});