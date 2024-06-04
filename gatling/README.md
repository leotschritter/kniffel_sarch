# Load Testing Kniffel with Gatling

## Usage
### Run full test suite
``sbt Gatling/test``
### Run a single test
``sbt 'Gatling/testOnly it.persistence.PersistenceVolumeItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceEnduaranceItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceLoadItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceSpikeItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceStressItSimulation'``

### Run a single diceCup test
``sbt 'Gatling/testOnly it.dicecup.DiceCupVolumeItSimulation'``

``sbt 'Gatling/testOnly it.dicecup.DiceCupEnduaranceItSimulation'``

``sbt 'Gatling/testOnly it.dicecup.DiceCupLoadItSimulation'``

``sbt 'Gatling/testOnly it.dicecup.DiceCupSpikeItSimulation'``

``sbt 'Gatling/testOnly it.dicecup.DiceCupStressItSimulation'``

