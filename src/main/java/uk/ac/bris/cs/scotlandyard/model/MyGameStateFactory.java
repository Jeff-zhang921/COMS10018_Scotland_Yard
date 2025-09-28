package uk.ac.bris.cs.scotlandyard.model;



//Outer Class: MyGameStateFactory:this is for creating a game instance
//Inner Class: MyGameState\
//This initializes the game state
//factory method
//MyGameStateFactory acts as the creator. by using .build
//Product: MyGameState




//makeSingleMoves(...) → Computes possible single moves.
//makeDoubleMoves(...) → Computes possible double moves for Mr.X
//getAvailableMoves(): Determines all possible moves for the current turn.


//getWinner():Determines if the game has ended everyturn
//advance(Move move)
//The concrete elements in this case are the move classes:
//SingleMove
//DoubleMove

//create an intial game state


//immutableset and hashset cannot allow duplicate element
//hashset can modified but not in order


//dynamic dispatch: enables method overriding which method in which concrete element to use
//









import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.io.Serializable;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * cw-model
 * stage 1: Complete this class
 */

public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		//private attributes
		//setup is every turn the gamestate
		private GameSetup setup;
		// holds the pieces that need to have their turn
		private ImmutableSet<Piece> remaining;
		// holds MrX's log entries
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		// holds the list of valid moves
		private ImmutableSet<Move> moves;



		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives) {
			//todo
			this.setup = setup;
			this.mrX = mrX;
			this.remaining = remaining;
			this.log = log;
			this.detectives = detectives;
			this.moves = getAvailableMoves();
			// check to see if setup is null
			if (log == null) {
				throw new NullPointerException();
			}

			if (remaining == null) {
				throw new NullPointerException();
			}

			//check mrx
			if (mrX == null) {
				throw new NullPointerException();
			}
			if (!mrX.isMrX()) {
				throw new IllegalArgumentException();
			}


			//check the detective
			if (detectives == null) {
				throw new NullPointerException();
			}


			List<Integer> integer = new ArrayList<>();
           //check if location has occupied
			for (Player player : detectives) {
				int b = player.location();
				if (integer.contains(b)) {
					throw new IllegalArgumentException();
				} else {
					integer.add(b);
				}

			}
			//check if has illigle ticket should throw
			for (Player player : detectives) {

				ImmutableMap<ScotlandYard.Ticket, Integer> c = player.tickets();
				if (c.get(ScotlandYard.Ticket.SECRET) != 0) {
					throw new IllegalArgumentException();
				}
				if (c.get(DOUBLE) != 0) {
					throw new IllegalArgumentException();
				}
			}

           //empty move should throw
		if (setup.moves.isEmpty()) {
				throw new IllegalArgumentException();
			}
       //empty graph should throw
		if (setup.graph.nodes().isEmpty()) {
				throw new IllegalArgumentException();
			}

		}





		// helper methods for get available moves

		// generate all the single moves得到了哪些点
        //分析可能走的点,没说要动
		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			//create a set for all possible available place to move
			Set<Move.SingleMove> singleMoves = new HashSet<>();
			//for every node that might go to
			for (int destination : setup.graph.adjacentNodes(source)) {
				//create a set for occupied location
				List<Integer> occupied = new ArrayList<>();
				for (Player detective : detectives) {
					occupied.add(detective.location());
				}
				//if the location didn't be occupied
				if (!occupied.contains(destination)) {

					//if it is mrX turn and mrX want use secrete ticket, secrete ticket can be any ticket so go to destination directly without need to ask wether has required ticket(secrete ticket can be any ticket)
					if (player.isMrX()) {
						if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)) {
							singleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
						}
					}
					for (ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
						//if the player from the input has the required ticket
						if (player.has(t.requiredTicket())) {
							//add to the array
							//move.singlemove is the possible destination
							singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}

				}
			}
			return singleMoves;
		}



		// generate the double moves for MrX
		// had to make singlemoves so we could access ticket
		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, Set<Move.SingleMove> SingleMoves) {
			//create a new set for doublemoves
			Set<Move.DoubleMove> DoubleMoves = new HashSet<>();
			//get single move from input

			for (Move.SingleMove first : SingleMoves) {
				//每进去一个create a set for second singlemoves
				Set<Move.SingleMove> secondlocation = new HashSet<>();
				//get the information of the after first move
				int originalLocation = first.source();
				ScotlandYard.Ticket firstTicket = first.ticket;
				int firstdestination = first.destination;
				//use the ticket move to middle location
				player = player.use(firstTicket);

				//get all the possible move store in to the set of second singlemoves
				secondlocation.addAll(makeSingleMoves(setup, detectives, player, firstdestination));
				//return the ticket to player cause it only use for calculate the possible location to move not real move
				player = player.give(firstTicket);
				//store all the second move (which second move already store the firstmove) to the double move
				for (Move.SingleMove second : secondlocation) {
					DoubleMoves.add(new Move.DoubleMove(player.piece(), originalLocation, firstTicket, firstdestination, second.ticket, second.destination));
				}
			}
			return DoubleMoves;
		}


		@Nonnull
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move.SingleMove> singleMoves = new HashSet<>();
			Set<Move.DoubleMove> doubleMoves = new HashSet<>();
			// if there is a x, then analyze x
			if (remaining.contains(mrX.piece())) {
				// analyse available move if x has single ticket
				singleMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				if (mrX.has(DOUBLE)) {
					mrX = mrX.use(DOUBLE);

					//setup.move is the round setting, such as 24 in the game
 				if (setup.moves.size()>1) {
					 if (mrX.tickets().size() > 1) {

							doubleMoves.addAll(makeDoubleMoves(setup, detectives, mrX, singleMoves));
						}
					}
					mrX = mrX.give(DOUBLE);
				}
			}
			// if there not x then detectives turn
			else {
				for (Player det : detectives) {
					if (remaining.contains((det.piece()))) {
						singleMoves.addAll(makeSingleMoves(setup, detectives, det, det.location()));
					}
				}
			}
			// return all the moves singleMoves + doubleMoves
			Set<Move> allMoves = new HashSet<>();
			allMoves.addAll(singleMoves);
			allMoves.addAll(doubleMoves);
			return ImmutableSet.copyOf(allMoves);

		}





		// implementation of the board interface
		// getters
		@Nonnull
		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			// create a set to store all the players
			Set<Piece> player = new HashSet<>();
			// store mrX
			//add all the pieces to the set
			player.add(mrX.piece());
			// stall all detectives
			for (Player det : detectives) {
				// add all players in the set
				player.add(det.piece());
			}
			// return the set
			return ImmutableSet.copyOf(player);
		}


		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			//looping through to get the detective inside the Player therefore can access the location in player
			// iterate through the list of detectives to determine which player this detective is
			for (Player det : detectives) {
				if (det.piece().equals(detective)) {
					return Optional.of(det.location());
				}
			}
			return Optional.empty();
		}




		//create a anonmous object to get the ticket from mrx
		//thinking: it need to return Board.ticketboard
		TicketBoard board = new TicketBoard() {
			@Override
			public int getCount(ScotlandYard.Ticket ticket) {

				int typesize=mrX.tickets().get(ticket);
				return typesize;
			}
		};
		@Nonnull
		@Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {

			if (piece.isMrX()) {
				return Optional.of(board);
			} else {
				for (Player det : detectives) {
					if (det.piece().equals(piece)) {
						// Create a new anonmous class TicketBoard for the detective:
						TicketBoard detboard = new TicketBoard() {
							@Override
							public int getCount(@Nonnull ScotlandYard.Ticket ticket) {

								int typesize=det.tickets().get(ticket);
								return typesize;
							}
						};
						return Optional.of(detboard);
					}
				}
			}
			return Optional.empty();
		}


		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {

			return log;
		}


		@Nonnull
		@Override
		// returns the wining pieces or empty if no winner
		public ImmutableSet<Piece> getWinner() {
			Set<Piece> detective=new HashSet<>();
			ImmutableSet remain= ImmutableSet.copyOf(remaining);
			//put the detective in this turn to an array
			for(Player det:detectives){
				detective.add(det.piece());
			}

			//the detective share same location with mrX
			for(Player det:detectives){
				if(mrX.location()==det.location()){
					remaining=ImmutableSet.of();
					return ImmutableSet.copyOf(detective);
				}

			}
			//copyof is to copy the list of (all in this turn)detective to the remaining player in this turn
			//detective cannot move due to....
            //make the remaining become the *detective* and check whether is win
			remaining=ImmutableSet.copyOf(detective);
				if(getAvailableMoves().isEmpty()){
					//clear remaining and get the winner
					remaining=ImmutableSet.of();
					return ImmutableSet.of(mrX.piece());
				}


			//mrX cannot move due to outside is filled with detective
            //make remaining *mrX*
			remaining=ImmutableSet.of(mrX.piece());
			if (getAvailableMoves().isEmpty()) {
				remaining=ImmutableSet.of();
				return ImmutableSet.copyOf(detective);
			}

		if(setup.moves.size()==log.size()){
		 remaining=ImmutableSet.of();
			return ImmutableSet.of(mrX.piece());
		  }
			//if there is no winner in this turn
           remaining=remain;
			return ImmutableSet.of();
		}



		// implementation of the gamestate interface
		@Override
		public GameState advance(Move move) {


			if (!moves.contains(move)) {
				throw new IllegalArgumentException();
			}




			//hiddenorreveal, updatestate,giveticket
           //singleMove is a element, visitor pattern is for different behaviours for applying SingleMoves and DoubleMove

			class Visitor implements Move.Visitor<GameState> {
				@Override
				public GameState visit(Move.SingleMove singleMove) {
					List<LogEntry> historyLog = new ArrayList<>(log);
					// store the state of all detectives
					List<Player> arrayDet = new ArrayList<>();
					// store which detectives can move remaining
					List<Piece> arrayRemaining = new ArrayList<>();
					//if remaining has mrX then this turn is mrX
					if (remaining.contains(mrX.piece())) {
						// Calculate the current log size, decide whether to expose the location
						//setup.move is a list of boolean for example rule says in 3, 8it reveal then the third and eight element of the list is true and other is false
						if (setup.moves.get(historyLog.size())) {
							LogEntry log1 = LogEntry.reveal(singleMove.ticket, singleMove.destination);
							historyLog.add(log1);
						}
						else {
							LogEntry log2 = LogEntry.hidden(singleMove.ticket);
							historyLog.add(log2);
						}

						// update MrX state
						mrX = mrX.use(singleMove.ticket);
						mrX = mrX.at(singleMove.destination);

						// update detective list, check whether they have any ticket
						//if have then add to array of remaining
						for (Player det : detectives) {
							//all detective add to detective list
							arrayDet.add(det);


							if (det.has(TAXI) || det.has(BUS) || det.has(UNDERGROUND)) {
								arrayRemaining.add(det.piece());
							}


						}
					}
					else {
						// detectives turn
						//dets is the currently moving detective
						Piece dets = singleMove.commencedBy();
						for (Player det : detectives) {


							if (dets.equals(det.piece())) {
								det = det.at(singleMove.destination);
								//use the ticket
								det = det.use(singleMove.ticket);
								//give the ticket to mrX
								mrX = mrX.give(singleMove.ticket);
							} else {
								//the detective that can move but is not it turn then add to array remaining
								if (remaining.contains(det.piece()) && (det.has(TAXI) || det.has(BUS) || det.has(UNDERGROUND))) {
									arrayRemaining.add(det.piece());
								}
							}
							//must put in the end of for loop cause the state of might det might change
							//the detective is dets
							arrayDet.add(det);
						}

						// if all detectives are finished, then MrX turn
						if (arrayRemaining.isEmpty()) {
							arrayRemaining.add(mrX.piece());
						}
					}

					// return a new game state
					return new MyGameState(setup, ImmutableSet.copyOf(arrayRemaining), ImmutableList.copyOf(historyLog), mrX, arrayDet);
				}

				@Override
				@Nonnull
				public GameState visit(Move.DoubleMove move) {

					// copy remaining detectives
					List<Piece> arrayRemaining = new ArrayList<>();
					// copy log
					List<LogEntry> historyLog = new ArrayList<>(log);

					// add the first step to log
					if (setup.moves.get(historyLog.size())) {
						historyLog.add(LogEntry.reveal(move.ticket1, move.destination1));
					} else {
						historyLog.add(LogEntry.hidden(move.ticket1));
					}
					mrX = mrX.use(move.ticket1);
					mrX = mrX.at(move.destination1);
					// add the second step to log
					if (setup.moves.get(historyLog.size())) {
						historyLog.add(LogEntry.reveal(move.ticket2, move.destination2));
					} else {
						historyLog.add(LogEntry.hidden(move.ticket2));
					}
					mrX = mrX.use(move.ticket2);
					mrX = mrX.use(DOUBLE);
					mrX = mrX.at(move.destination2);

					// calculate detective who still has ticket
					for (Player det : detectives) {
						if (det.has(TAXI) || det.has(BUS) || det.has(UNDERGROUND)) {
							arrayRemaining.add(det.piece());
						}
					}

					// return new game state
					return new MyGameState(setup, ImmutableSet.copyOf(arrayRemaining), ImmutableList.copyOf(historyLog), mrX, detectives);

				}
			}



           //visitor has two method it is not mrX or detective is visitor it is like a referee
			//move contains singlemove or doublemove
			Visitor Move = new Visitor();
			return move.accept(Move);

		}

	}
		@Nonnull@Override

		public GameState build(
				GameSetup setup,
				Player mrX,
				ImmutableList<Player> detectives) {

			return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);

		}
}