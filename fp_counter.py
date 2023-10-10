import sys

def split_mr(mr):
    precond = mr.split(' -> ')[0]
    precond = precond[1:len(precond)-1]
    preconds = precond.split(', ')
    mrs = []
    for cond in preconds:
        mrs.append(f'[{cond}] -> {mr.split(" -> ")[1]}')
    return mrs

def split_mrs(mrs):
    split_mrs = []
    for mr in mrs:
        split_mrs.extend(split_mr(mr))
    return split_mrs

subject = sys.argv[1]
seed = sys.argv[2]
f1 = f'output/{subject}/allow_epa_loops_true/EPA_AWARE/1000/{seed}/mrs-alloy-predicates.als'
f2 = sys.argv[3]

lines1 = set()
with open(f1) as file:
    for line in file.readlines():
        lines1.add(line.split(' # ')[0])

lines2 = set()
with open(f2) as file:
    for line in file.readlines():
        lines2.add(line.split(' # ')[0])

lines1 = set(split_mrs(lines1))
lines2 = set(split_mrs(lines2))


print('\nFalse positive properties: \n')
#diff = lines1.symmetric_difference(lines2)
diff = set(lines2) - set(lines1)
print(*diff, sep='\n')
print(f'\nCount: {len(diff)}')
