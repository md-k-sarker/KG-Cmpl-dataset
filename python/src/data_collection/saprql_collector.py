from SPARQLWrapper import SPARQLWrapper, JSON, XML, N3, RDF, CSV, TSV

# sparql = SPARQLWrapper("http://dbpedia.org/sparql")
# sparql.setQuery("""
#     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
#     SELECT ?label
#     WHERE { <http://dbpedia.org/resource/Asturias> rdfs:label ?label }
# """)
#
#
# sparql.setReturnFormat(XML)
# results = sparql.query().convert()
#
#
# print(results.toxml())

#
# for result in results["results"]["bindings"]:
#     print(result)


from SPARQLWrapper import SPARQLWrapper, XML

# uri = "http://dbpedia.org/resource/Asturias"
query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } LIMIT 10"
# % (uri, uri)

sparql = SPARQLWrapper("http://dbpedia.org/sparql")
sparql.setQuery(query)
sparql.setReturnFormat(XML)
results = sparql.query().convert()

file = open("output_data/output.rdf", "wb")
results.serialize(destination=file, format="xml")
file.flush()
file.close()