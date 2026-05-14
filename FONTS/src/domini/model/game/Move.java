package domini.model.game;

import domini.model.cell.Position;
import java.io.Serializable;

public record Move(Position position, int previousValue, int newValue) implements Serializable {}
