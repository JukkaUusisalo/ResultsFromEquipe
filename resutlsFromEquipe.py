#!/usr/bin/python3

import http.client
import json
import sys, getopt

def main(argv):
    seura = None
    try:
        opts, args = getopt.getopt(argv,"hs:",["seura="])
    except getopt.GetoptError:
        print ('resultsFromEquipe.py -s <seuran nimi>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
             print ('resultsFromEquipe.py -s <seuran nimi>')
             sys.exit()
        elif opt in ("-s", "--seura"):
            seura = arg
    if(seura is None):
        print ('resultsFromEquipe.py -s <seuran nimi>')   
        sys.exit(2) 
    connection = http.client.HTTPSConnection('online.equipe.com')
    headers = {'Content-type': 'application/json'}
    connection.request('GET', '/api/v1/meetings/recent', headers=headers)
    response = connection.getresponse()
    competitions = json.loads(response.read().decode())
    for competition in competitions:
        competitionName = competition['display_name']
        competitionId = competition['id']
        venueCountry = competition['venue_country']
        if(venueCountry=='FIN'):
            apiCall = '/api/v1/meetings/{}/schedule'.format(competitionId)
            connection.request('GET', apiCall, headers=headers)
            response = connection.getresponse()
            competitionJson = json.loads(response.read().decode())
            for days in competitionJson['days']:
                for meetingClass in days['meeting_classes']:
                    classId = meetingClass['id']
                    className = meetingClass['name']        
                    if "class_sections" in meetingClass:        
                        classSections = meetingClass['class_sections']
                        for section in classSections:
                                sectionId = section['id']
                                apiCall = '/api/v1/class_sections/{}'.format(sectionId)
                                connection.request('GET', apiCall, headers=headers)
                                response = connection.getresponse()
                                sectionJson = json.loads(response.read().decode()) 
                                results = sectionJson['starts']
                                for result in results:
                                    rank = result['rank']
                                    riderName = result['rider_name']
                                    horseName = result['horse_name']
                                    clubName = result['club_name']
                                    placed = False
                                    if 'placed' in result:
                                        placed = result['placed']
                                    if(rank is not None and (rank <4 or placed) and clubName is not None and clubName.startswith(seura) ):
                                        print(competitionName)
                                        print('-- {}'.format(className))
                                        print('{}. {}/{} {}'.format(rank,riderName,horseName,clubName))


if __name__ == "__main__":
   main(sys.argv[1:])

