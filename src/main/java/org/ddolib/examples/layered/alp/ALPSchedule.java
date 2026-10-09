package org.ddolib.examples.layered.alp;

/**
 * Contains schedule data for an aircraft.
 *
 * @param aircraft the id of the aircraft
 * @param aircraftClass the class of the aircraft
 * @param landingTime when the aircraft is landing
 * @param runway the runway on which the aircraft is landing
 */
record ALPSchedule(int aircraft, int aircraftClass, int landingTime, int runway) {}
