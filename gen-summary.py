import os
import subprocess
import csv

header = ['Class', 'Input', 'Output', 'Accept %', 'Reject %']
rows = []

dir_path = 'output/'
for filename in os.listdir(dir_path):
    if '.txt' in filename:
        with open(dir_path + filename) as f:
            lines = f.readlines()
            clazz = [line for line in lines if 'Class: ' in line][0].split(': ')[1].strip()
            accept_percentage = float([line for line in lines if '% of valid MRs: ' in line][0].split(': ')[1])
            reject_percentage = float([line for line in lines if '% of invalid MRs: ' in line][0].split(': ')[1])
            input1 = [line.strip() for line in lines if 'Input: ' in line][0].split(': ')[1].split(' ')[1]
            if not input1:
                input1 = 0
            else:
                input1 = int(input1)
            output = [line.strip() for line in lines if 'Output: ' in line][0].split(': ')[1].split(' ')[0]
            if not output:
                output = 0
            else:
                output = int(output)

            rows.append([clazz, input1, output, accept_percentage, reject_percentage])

filename = "output/output.csv"
    
with open(filename, 'w') as csvfile: 
    csvwriter = csv.writer(csvfile) 
    csvwriter.writerow(header) 
    csvwriter.writerows(rows)
