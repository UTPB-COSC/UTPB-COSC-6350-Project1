import pandas as pd
import json
import csv

# Load the uploaded CSV file to examine its structure
file_path1 = 'Wifi_Networks.csv' #Gabriel phone 1
file_path2 = "atp6DuChen.csv"    #Du Chen phone2 
wifi_data1 = pd.read_csv(file_path1)
wifi_data2 = pd.read_csv(file_path2)

# Display the first few rows of the file to understand its structure
wifi_data1.head()
wifi_data1.columns
wifi_data2.head()
wifi_data2.columns

# Extract the encryption type from the 'description' column
wifi_data1['Encryption'] = wifi_data1['description'].str.extract(r'Encryption:\s*(\w+)')
wifi_data2['Encryption'] = wifi_data2['description'].str.extract(r'Encryption:\s*(\w+)')

# Get counts of each encryption type to verify the data
print("Data from phone 1: ")
encryption_counts1 = wifi_data1['Encryption'].value_counts()
print (encryption_counts1)
# Calculate the total number of Wi-Fi networks for each phone
total_networks_phone1 = encryption_counts1.sum()
print("Total networks detected by phone 1:", total_networks_phone1)
print()

print("Data from phone 2: ")
encryption_counts2 = wifi_data2['Encryption'].value_counts()
print (encryption_counts2)
total_networks_phone2 = encryption_counts2.sum()
print("Total networks detected by phone 2:", total_networks_phone2)
print()


#Getting data that only has encryption of None, Unknown, None, or WPA
wifi_data1[['X', 'Y', 'Name', 'Encryption']].head(), encryption_counts1
wifi_data2[['X', 'Y', 'Name', 'Encryption']].head(), encryption_counts2


# Filter the data for the two categories
category_none_WEP1 = wifi_data1[wifi_data1['Encryption'].isin(['None', 'Unknown', 'WEP'])]
category_WPA1 = wifi_data1[wifi_data1['Encryption'] == 'WPA']

category_none_WEP2 = wifi_data2[wifi_data2['Encryption'].isin(['None', 'Unknown', 'WEP'])]
category_WPA2 = wifi_data2[wifi_data2['Encryption'] == 'WPA']

#Extra category for WPA2
category_WPA2_one = wifi_data1[wifi_data1['Encryption'] == 'WPA2']

category_WPA2_two = wifi_data2[wifi_data2['Encryption'] == 'WPA2']

#Extra category from the second phone
category_WPA3 = wifi_data2[wifi_data2['Encryption'] == 'WPA3']

# Prepare the data in a format for JavaScript
data = {
    "none_WEP_1": category_none_WEP1[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "WPA_1": category_WPA1[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "none_WEP_2": category_none_WEP2[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "WPA_2": category_WPA2[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "WPA2_1": category_WPA2_one[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "WPA2_2": category_WPA2_two[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
    "WPA3": category_WPA3[['Name', 'X', 'Y', 'Encryption']].to_dict(orient='records'),
}

# Output the modified data as JS file content
with open('wifi_data.js', 'w') as f:
    f.write(f"const data = {data};")

# For WiGle data
# Read the CSV file
with open('Wifi_Networks.csv', 'r') as csv_file:
    csv_reader = csv.DictReader(csv_file)
    data = [row for row in csv_reader]

# Write to JSON file
with open('wifi_data.json', 'w') as json_file:
    json.dump(data, json_file, indent=4)
