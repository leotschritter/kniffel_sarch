package de.htwg.sa.kniffel.persistence

import de.htwg.sa.kniffel.persistence.api.PersistenceApi
import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import de.htwg.sa.kniffel.persistence.persistence.slickImpl.SlickDAO

object PersistenceService:
  val persistence: IPersistence = new SlickDAO()
  given IPersistence = persistence


  @main def main(): Unit = PersistenceApi().start
