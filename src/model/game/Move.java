package model.game;

import model.cell.Position;
import java.io.Serializable;

public record Move(Position position, int previousValue, int newValue) implements Serializable {}
