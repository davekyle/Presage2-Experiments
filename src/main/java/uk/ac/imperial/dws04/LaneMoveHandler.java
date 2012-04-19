package uk.ac.imperial.dws04;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.location.LocationService;
import uk.ac.imperial.presage2.util.location.Move;
import uk.ac.imperial.presage2.util.location.MoveHandler;
import uk.ac.imperial.presage2.util.location.area.AreaService;
import uk.ac.imperial.presage2.util.location.area.EdgeException;
import uk.ac.imperial.presage2.util.location.area.HasArea;

import com.google.inject.Inject;

/**
 * Special move handler to deal with moves in an area representing a multi-lane
 * road. Adds collision detection on top of moves in a Cell area.s
 * 
 * @author Sam Macbeth
 * 
 */
@ServiceDependencies({ AreaService.class, LocationService.class })
public class LaneMoveHandler extends MoveHandler {

	private final Logger logger = Logger.getLogger(LaneMoveHandler.class);
	private List<CollisionCheck> checks = new LinkedList<LaneMoveHandler.CollisionCheck>();

	@Inject
	public LaneMoveHandler(HasArea environment,
			EnvironmentServiceProvider serviceProvider,
			EnvironmentSharedStateAccess sharedState, EventBus eb)
			throws UnavailableServiceException {
		super(environment, serviceProvider, sharedState);
		eb.subscribe(this);
	}

	@EventListener
	public void checkForCollisions(EndOfTimeCycle e) {
		for (CollisionCheck c : checks) {
			c.checkForCollision();
		}
		checks.clear();
	}

	@Override
	public boolean canHandle(Action action) {
		return action instanceof CellMove;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		CellMove m = (CellMove) action;

		// TODO - requires environment services for agents' max speed etc.
		// check move forward magnitude is <= actor's max speed
		// check move forward change in magnitude <= actor's max
		// acceleration/deceleration

		// check move sideways magnitude is 0 or 1
		if (Math.abs(m.getX()) > 1) {
			throw new ActionHandlingException(
					"Cannot change greater than one lane at once. Move was: "
							+ m);
		}

		RoadLocation start = (RoadLocation) locationService
				.getAgentLocation(actor);
		RoadLocation target = new RoadLocation(start.add(m));

		if (!target.in(environment.getArea())) {
			try {
				final Move mNew = environment.getArea().getValidMove(start, m);
				target = new RoadLocation(start.add(mNew));
			} catch (EdgeException e) {
				throw new ActionHandlingException(e);
			}
		}
		this.locationService.setAgentLocation(actor, target);
		checks.add(new CollisionCheck(start, target));

		logger.info(actor + " move: " + m);
		return null;
	}

	class CollisionCheck {
		final RoadLocation startFrom;
		final RoadLocation finishAt;
		Set<UUID> collisionCandidates = new HashSet<UUID>();
		Map<UUID, RoadLocation> candidateLocs = new HashMap<UUID, RoadLocation>();
		final boolean laneChange;
		final int areaLength = areaService.getSizeY();

		public CollisionCheck(RoadLocation startFrom, RoadLocation finishAt) {
			super();
			this.startFrom = startFrom;
			this.finishAt = finishAt;

			// build collision candidate set
			int startOffset = startFrom.getOffset();
			int finishOffset = finishAt.getOffset();
			if (finishOffset < startOffset)
				finishOffset += areaLength;
			for (int lane = 1; lane < areaService.getSizeX(); lane++) {
				collisionCandidates.addAll(getAgentsInLane(lane,
						startOffset + 1, finishOffset));
			}
			laneChange = startFrom.getLane() != finishAt.getLane();
			for (UUID a : collisionCandidates) {
				candidateLocs.put(a,
						(RoadLocation) locationService.getAgentLocation(a));
			}
		}

		private Set<UUID> getAgentsInLane(int lane, int from, int to) {
			Set<UUID> agents = new HashSet<UUID>();
			for (int y = from; y <= to; y++) {
				agents.addAll(areaService.getCell(lane, y % areaLength, 0));
			}
			return agents;
		}

		public boolean checkForCollision() {
			boolean collisionOccured = false;
			Set<UUID> agentsOnCurrentCell = areaService.getCell(
					finishAt.getLane(), finishAt.getOffset(), 0);
			if (agentsOnCurrentCell.size() > 1) {
				logger.warn("Collision Occurred: Multiple agents on one cell. Cell: "
						+ this.finishAt + ", agents: " + agentsOnCurrentCell);
				collisionOccured = true;
			}
			for (UUID a : collisionCandidates) {
				RoadLocation current = (RoadLocation) locationService
						.getAgentLocation(a);
				if (current.getLane() == finishAt.getLane()) {
					// same lane, if he is behind us then it is a collision
					int hisOffset = current.getOffset();
					if (hisOffset < candidateLocs.get(a).getOffset())
						hisOffset += areaLength;
					int myOffset = finishAt.getOffset();
					if (myOffset < startFrom.getOffset())
						myOffset += areaLength;
					if (hisOffset < myOffset) {
						logger.warn("Collision Occured: Agent "
								+ agentsOnCurrentCell + " went through " + a);
						collisionOccured = true;
					}
				}
				if (laneChange && current.getLane() == startFrom.getLane()
						&& finishAt.getLane() == candidateLocs.get(a).getLane()) {
					logger.warn("Collision Occured: Agent "
							+ agentsOnCurrentCell + " crossed paths with " + a);
					collisionOccured = true;
				}
			}
			return collisionOccured;
		}

	}

}
