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

### Run a single game test
``sbt 'Gatling/testOnly it.game.GameVolumeItSimulation'``

``sbt 'Gatling/testOnly it.game.GameEnduranceItSimulation'``

``sbt 'Gatling/testOnly it.game.GameLoadItSimulation'``

``sbt 'Gatling/testOnly it.game.GameSpikeItSimulation'``

``sbt 'Gatling/testOnly it.game.GameStressItSimulation'``

### Run a single field test
``sbt 'Gatling/testOnly it.field.FieldVolumeItSimulation'``

``sbt 'Gatling/testOnly it.field.FieldEnduranceItSimulation'``

``sbt 'Gatling/testOnly it.field.FieldLoadItSimulation'``

``sbt 'Gatling/testOnly it.field.FieldSpikeItSimulation'``

``sbt 'Gatling/testOnly it.field.FieldStressItSimulation'``