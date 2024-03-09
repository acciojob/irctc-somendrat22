package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        Train train = new Train();
        //Here we need to cover the trainDto
        String route = "";
        for(Station station : trainEntryDto.getStationRoute()){
            route = route + station.toString() + ",";
        }
        StringBuffer sb= new StringBuffer(route);
        //invoking the method
        sb.deleteCharAt(sb.length()-1);

        train.setRoute(sb.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        train = trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        int totalSeats = train.getNoOfSeats();

        int bookings = 0;

        String route = train.getRoute();

        int boardingStationIndex = route.indexOf(seatAvailabilityEntryDto.getFromStation().toString());
        int destinationStationIndex = route.indexOf(seatAvailabilityEntryDto.getToStation().toString());

        for(Ticket ticket :train.getBookedTickets()){

            int startIndexOfTicket = route.indexOf(ticket.getFromStation().toString());
            int endIndexOfTicket = route.indexOf(ticket.getToStation().toString());

            if ((startIndexOfTicket < destinationStationIndex && startIndexOfTicket >= boardingStationIndex) ||
                    (endIndexOfTicket > boardingStationIndex && endIndexOfTicket <= destinationStationIndex) ||
                    (startIndexOfTicket <= boardingStationIndex && endIndexOfTicket >= destinationStationIndex)) {
                bookings += ticket.getPassengersList().size();
            }
        }
        int remainingSeats = totalSeats - bookings;
        return remainingSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");

        String boardingStation = station.toString();

        Train train = trainRepository.findById(trainId).get();

        if(!train.getRoute().contains(boardingStation))
        {
            throw new Exception("Train is not passing from this station");
        }
        List<Ticket> bookedTickets = train.getBookedTickets();
        int count = 0;
        for(Ticket ticket : bookedTickets){

            if(ticket.getFromStation().toString().equals(boardingStation)){
                count = count + ticket.getPassengersList().size();
            }
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();
        //We have got the age of the oldest person that is travelling the train
        List<Ticket> bookedTickets = train.getBookedTickets();

        int maxAge = 0;
        for(Ticket ticket : bookedTickets){

            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger : passengerList){
                maxAge = Math.max(maxAge,passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        int startMin = startTime.getHour()*60 + startTime.getMinute();
        int endMin = endTime.getHour()*60 + endTime.getMinute();
        List<Train> trains = trainRepository.findAll();
        List<Integer> trainIdList = new ArrayList<>();
        String currentStation = station.toString();
        for(Train train : trains){

            //Once we get the train and then for each Train we need to do this :
            String route = train.getRoute();
            String[] result = route.split(",");

            int extraHours = 0;
            for(String st : result){
                if(st.equals(currentStation)){
                    break;
                }
                extraHours ++;
            }

            int totalHours = train.getDepartureTime().getHour() + extraHours;
            int totalMinutes = train.getDepartureTime().getMinute();


            int time = totalHours*60 + totalMinutes;

            if(time>=startMin && time <= endMin){
                trainIdList.add(train.getTrainId());
            }

        }
        return trainIdList;
    }

}