# Load Testing Kniffel with Gatling

## Usage
### Run full test suite
``sbt Gatling/test``
### Run a single test
``sbt 'Gatling/testOnly it.persistence.PersistenceBaseItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceEnduaranceItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceLoadItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceSpikeItSimulation'``

``sbt 'Gatling/testOnly it.persistence.PersistenceStressItSimulation'``

