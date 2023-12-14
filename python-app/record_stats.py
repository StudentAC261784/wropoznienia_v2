from datetime import datetime
from time import sleep
import pandas as pd
from firebase_admin import credentials, initialize_app, firestore

# sudo systemctl daemon-reload
# sudo systemctl restart runstats247
# sudo systemctl status runstats247

def initialize_firestore():
    cred = credentials.Certificate("firebaseadminkey.json")
    initialize_app(cred, {'storageBucket': 'wropoznienia-401812.appspot.com', 'databaseURL': 'https://wropoznienia-401812.firebaseio.com/'})
    return firestore.client().collection(u'avg_delays')

def main():
    new_start = True
    new_route_id = False

    results = pd.DataFrame(columns=['route_id', 'avg_delays'])
    minute_counter = 0

    doc_ref = initialize_firestore()

    while True:
        current_time = datetime.now()
        if current_time.minute % 5 == 0:
            source_data = None
            selected_source_rows = None
            
            source_data = pd.read_csv(f'data/vehicles_data.csv')
            if len(source_data) > 0:
                unique_route_ids = sorted(source_data['route_id'].unique().tolist())

                minutes_since_midnight = current_time.hour * 60 + current_time.minute
                list_index = int(minutes_since_midnight / 5)
                    
                for route_id in unique_route_ids:
                    selected_source_rows = source_data[source_data['route_id'] == route_id]
                    avg_delay_value = int(selected_source_rows['delay_seconds'].mean())
                    try:
                        index_to_update = results.index[results['route_id'] == route_id].tolist()[0]
                    except IndexError:
                        # results = pd.concat([results, pd.DataFrame({'route_id': route_id, 'avg_delays': None})], ignore_index=True)
                        results.loc[len(results)] = {'route_id': route_id, 'avg_delays': None}
                        index_to_update = results.index[results['route_id'] == route_id].tolist()[0]
                        new_route_id = True
                    if new_start or new_route_id:
                        new_delay_list = [''] * 288
                        new_delay_list[list_index] = str(avg_delay_value)
                        new_delay_string = ";".join(new_delay_list)
                        new_route_id = False
                    else:
                        new_delay_list = results.loc[index_to_update, 'avg_delays'].split(";")
                        new_delay_list[list_index] = str(avg_delay_value)
                        new_delay_string = ";".join(new_delay_list)

                    results.loc[index_to_update, 'avg_delays'] = new_delay_string

                new_start = False
                results.to_csv('data/avg_delays_stats.csv', encoding='utf-8-sig', index=False)
                minute_counter += 5
                if minute_counter == 30:
                    tmp = results.to_dict(orient='route_id')
                    list(map(lambda x: doc_ref.document(str(x['route_id'])).set({'avg_delays': x['avg_delays']}), tmp))
                    minute_counter = 0

                sleep(300)
            else:
                sleep(300)
        else:
            sleep(55)
            minute_counter = 0

if __name__ == "__main__":
    main()