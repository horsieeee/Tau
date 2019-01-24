package tau.runtime;

import tau.interpreter.*;
import java.util.*;

public interface ICallable {
	public int arity();
	public Object call(Interpreter interpreter,
			List<Object> arguments);
}
