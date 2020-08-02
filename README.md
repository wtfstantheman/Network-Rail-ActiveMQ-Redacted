# Network Rail ActiveMQ (Redacted)
A GUI Java implementation of accessing the Network Rail data feed built on https://github.com/wtfstantheman/Network-Rail-ActiveMQ-CLI

The program allows you to login to the Network Rail data feed and load configuration zip archives

The configuration archive contains:
- CONFIG.json
- SOP.json
- MAP.svg

# Building a Configuration zip archive

Building a **CONFIG.json** file, it needs to contain:
- Name (This will be displayed on the top window bar)
- Topic (The TD signalling area the client will connect to)
- Areas Covered (The TD signalling areas covered by the map)
  - ID (The TD area for the area)
  - Name (The name of the signalling area, usually the signalling full name and the stations at either end)
  - Topic (The specific signalling area which the area is in)
  
```
{
    "$type": "NetworkRail.ActiveMQ.Support.Config",
    "name": "Basingstoke (Poole - Wool)",
    "topic": "TD_ALL_SIG_AREA",
    "areas_covered": [
        {
            "id": "BP",
            "name": "Basingstoke (Poole - Wool)",
            "topic": "TD_WESS_SIG_AREA"
        }
    ]
}
```


Building a **SOP.json** file, it needs to contain:
- Area ID (The TD signaling area ID)
  - SOP
    - Address (The Signalling address)
      - Signal (The Signal ID)
      
```
{
    "$type": "NetworkRail.ActiveMQ.Support.SOP",
    "BP": {
        "SOP": {
            "0": {
                "0": "SPW5203",
                "1": "SPW5201",
                "2": "SPW5201",
                "3": "SPW5191",
                "4": "SPW5188",
                "5": "SPW5186",
                "6": "SPW5185",
                "7": "SPW5183"
            },
            "1": {
                "0": "SPW5740",
                "1": "SPW5693",
                "2": "SPW5690",
                "3": "SPW5216",
                "4": "SPW5215",
```

Building a **MAP.svg** file.
The map is a svg document with signals and berths by id

Example signal, the ID is built of the TD signaling area ID, the signal prefex and then the signal id:
BP SPW 5247
```
<circle cx="4555" cy="104" r="5" id="BPSPW5247"/>
```

Example berth, the ID is built of the TD signaling area ID and then the berth id.
BP 5248
```
<text id="BP5248" x="4636" y="149">XXXX</text>
```
