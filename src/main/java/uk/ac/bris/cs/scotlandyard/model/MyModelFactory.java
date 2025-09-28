package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event.GAME_OVER;
import static uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event.MOVE_MADE;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	public final class MyModel implements Model {
		Board.GameState currentBoard;
		HashSet<Observer> observers = new HashSet<>();

		private MyModel(
				GameSetup setup,
				Player mrX,
				ImmutableList<Player> detectives
		) {
           currentBoard=(new MyGameStateFactory()).build(setup,mrX,detectives);
		}

		@Override
		@Nonnull
		public Board getCurrentBoard() {
			return currentBoard;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observer == null) throw new NullPointerException();

			if (observers.contains(observer)) {
				throw new IllegalArgumentException();
			} else {
				observers.add(observer);
			}

		}

		@Override

		public void unregisterObserver(@Nonnull Observer observer) {

			if (observer == null) throw new NullPointerException();

			if (!observers.contains(observer)) {
				throw new IllegalArgumentException();
			} else observers.remove(observer);

		}

		@Override
		@Nonnull
		public ImmutableSet<Observer> getObservers() {

			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			currentBoard = currentBoard.advance(move);


			if (currentBoard.getWinner().isEmpty()) {
				for (Observer x : observers) {
					x.onModelChanged(currentBoard,  MOVE_MADE);
				}
				return;
			}

			else {
				for (Observer x : observers) {
					x.onModelChanged(currentBoard,GAME_OVER);

				}
				return;
			}

				// TODO Advance the model with move, then notify all observers of what what just happened.
				//  you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER
			}

		}

		@Nonnull
		@Override
		public Model build(GameSetup setup,
						   Player mrX,
						   ImmutableList<Player> detectives) {
			// TODO
			return new MyModel(setup,mrX,detectives);

		}
}

