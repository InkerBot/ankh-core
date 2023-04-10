package org.inksnow.ankh.jsnashorn;

import org.inksnow.ankh.core.api.script.AnkhScriptEngine;
import org.inksnow.ankh.core.api.script.PreparedScript;
import org.inksnow.ankh.core.api.script.ScriptContext;
import org.inksnow.ankh.core.script.ScriptCacheStack;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.inject.Singleton;
import javax.script.CompiledScript;
import javax.script.ScriptException;

@Singleton
public class J11JsNashornEngine implements AnkhScriptEngine {
  private static final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

  @Override
  public PreparedScript prepare(String script) throws Exception {
    return new NashornPps(script);
  }

  private static class NashornPps implements PreparedScript {
    private final String script;
    private final ScriptCacheStack<CompiledScript, ScriptException> cacheStack;

    public NashornPps(String script) throws ScriptException {
      this.script = script;
      this.cacheStack = new ScriptCacheStack<>(this::createScript);
      this.cacheStack.prepare(1);
    }

    private CompiledScript createScript() throws ScriptException {
      return createEngine().compile(script);
    }

    private NashornScriptEngine createEngine() {
      return (NashornScriptEngine) factory.getScriptEngine(
          new String[]{"--global-per-engine"},
          this.getClass().getClassLoader()
      );
    }

    @Override
    public Object execute(ScriptContext context) throws Exception {
      CompiledScript compile = cacheStack.borrow();
      try {
        return compile.eval(new JavaxScriptContext(context));
      } finally {
        cacheStack.sendBack(compile);
      }
    }
  }
}
