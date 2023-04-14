import re

with open('扬州美食.txt', 'r', encoding='utf-8') as f:
    data = f.read()

food_pattern = r'美食名称:(.*?) 美食介绍:(.*?) 店铺名称:(.*?) 思考'
foods = re.findall(food_pattern, data, re.S)

for food in foods:
    print(f'美食名称:{food[0]} 美食介绍:{food[1]} 店铺名称:{food[2]}')