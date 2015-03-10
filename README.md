# Welcome to the homepage of ROCKER - Refinement Operator Approach for Key Discovery! 

## Run from terminal ##

First, download the [full jar package](https://github.com/AKSW/rocker/releases/download/v1.2.1/rocker-1.2.1-full.jar), which also contains all required dependencies. Datasets are available here:

OAEI Benchmark 2011 (artificial data)

* [OAEI_2011_Restaurant_1.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/OAEI_2011_Restaurant_1.nt.gz)
* [OAEI_2011_Restaurant_2.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/OAEI_2011_Restaurant_2.nt.gz)

DBpedia 3.9 (real data)

* [album.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/album.nt.gz)
* [animal.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/animal.nt.gz)
* [architecturalStruture.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/architecturalStruture.nt.gz)
* [artist.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/artist.nt.gz)
* [careerstation.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/careerstation.nt.gz)
* [musicalWork.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/musicalWork.nt.gz)
* [organisationMember.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/organisationMember.nt.gz)
* [personFunction.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/personFunction.nt.gz)
* [soccerplayer.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/soccerplayer.nt.gz)
* [village.nt.gz](https://bitbucket.org/mommi84/rocker-servlet/downloads/village.nt.gz)

To run ROCKER:

```
java -Xmx8g -jar rocker-1.2.1-full.jar <dataset name> <dataset path with protocol> <class name> <find one key> <fast search> <alpha threshold>
```

Example:

```
java -Xmx8g -jar rocker-1.2.1-full.jar "restaurant_1" "file:///home/rocker/OAEI_2011_Restaurant_1.nt" "http://www.okkam.org/ontology_restaurant1.owl#Restaurant" false true 1.0
```

We recommend to run your experiments on a machine with at least 8 GB of RAM.

## Java library ##

You may also download the [Java library](https://github.com/AKSW/rocker/releases/download/v1.2.1/rocker-1.2.1.jar) without dependencies.

## Citing ROCKER ##

Please refer to the paper *T. Soru, E. Marx, A.-C. Ngonga Ngomo, "ROCKER - A Refinement Operator for Key Discovery"*, in proceedings of the 24th International Conference on World Wide Web, WWW 2015. [[pdf](http://svn.aksw.org/papers/2015/WWW_Rocker/public.pdf)]

