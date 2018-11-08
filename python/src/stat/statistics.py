import numpy as np
import os as os
import json as json


def read_stats(json_object):
    """

    :param json_object: json object from a file
    :return: rdf type triples etc
    """

    pass



def read_each_file(file_path):
    """

    :param file_path: file path of each json
    :return: json object from that file
    """
    with open(file_path) as f:
        data = json.load(f)
        # data is now a dictionary
        invLength = len(data["InvalidAxioms"])
        lengths.append(invLength)
        print("length of invalid axioms: ",invLength)



def iterate_over_files(dir_path):
    """

    :param dir_path: directory path to iterate
    :return: call read_each_file() function for each json file
    """
    for file_name in os.listdir(dir_path):
        print("working for : ", file_name)
        if file_name.endswith(".json"):
            read_each_file(os.path.join(dir_path, file_name))





dir_path = "/home/sarker/Desktop/data/jsons/json_synthetic"
lengths = []

if __name__ =="__main__":
    iterate_over_files(dir_path)
    print('avg: ', np.average(lengths))
