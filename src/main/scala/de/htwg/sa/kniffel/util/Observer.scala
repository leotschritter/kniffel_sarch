package de.htwg.sa.kniffel
package util

trait Observer:
  def update(e: Event): Unit

class Observable:
  var subscribers: Vector[Observer] = Vector()
  def add(s: Observer): Unit = subscribers = subscribers :+ s
  def remove(s: Observer): Unit = subscribers = subscribers.filterNot(o => o == s)
  def notifyObservers(e: Event): Unit = subscribers.foreach(o => o.update(e))

enum Event:
  case Quit
  case Load
  case Save
  case Move