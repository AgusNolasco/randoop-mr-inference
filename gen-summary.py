import os
import subprocess
import csv

header = ['Class', 'MRs', 'Accept %', 'Reject %']
rows = []

dir_path = 'output/'
for filename in os.listdir(dir_path):
    if '.txt' in filename:
        with open(dir_path + filename) as f:
            lines = f.readlines()
            clazz = [line for line in lines if 'Class: ' in line][0].split(': ')[1].strip()
            accept_percentage = float([line for line in lines if '% of valid MRs: ' in line][0].split(': ')[1])
            reject_percentage = float([line for line in lines if '% of invalid MRs: ' in line][0].split(': ')[1])

            valid_mrs = []
            copy = False
            for line in lines:
                if "Valid MRs: " in line:
                    copy = True
                    continue
                elif "Input: " in line:
                    copy = False
                    continue
                elif copy and line.strip():
                    valid_mrs.append(line.strip())

            rows.append([clazz, valid_mrs, accept_percentage, reject_percentage])

filename = "output/output.csv"
    
with open(filename, 'w') as csvfile: 
    csvwriter = csv.writer(csvfile) 
    csvwriter.writerow(header) 
    csvwriter.writerows(rows)
