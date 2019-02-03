package states;

import org.junit.Before;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FanForgeTest<Given extends Stage<Given>, When extends Stage<When>, Then extends Stage<Then>> {
    @Autowired
    private Given given;
    @Autowired
    private When when;
    @Autowired
    private Then then;
    
    private boolean givenRetrieved = false;
    private boolean whenRetrieved = false;
    private boolean thenRetrieved = false;
    
    @Before
    public void stageBefores() throws Exception {
        for (Method m : this.given.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Before.class)) continue;
            m.invoke(this.given);
        }
        for (Method m : this.when.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Before.class)) continue;
            m.invoke(this.when);
        }
        for (Method m : this.then.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Before.class)) continue;
            m.invoke(this.then);
        }
        this.given.reset();
        this.when.reset();
        this.then.reset();
    }
    
    @After
    public void stageAfters() throws Exception {
        for (Method m : this.given.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(After.class)) continue;
            m.invoke(this.given);
        }
        for (Method m : this.when.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(After.class)) continue;
            m.invoke(this.when);
        }
        for (Method m : this.then.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(After.class)) continue;
            m.invoke(this.then);
        }
    }
    
    protected Given given() throws Exception {
        if (this.givenRetrieved) return this.given;
        givenRetrieved = true;
        for (Method m : this.given.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(BeforeStage.class)) continue;
            m.invoke(this.given);
        }
        return this.given;
    }
    
    protected When when() throws Exception {
        if (this.whenRetrieved) return this.when;
        this.whenRetrieved = true;
        
        if (!givenRetrieved) throw new IllegalStateException("Must call given() before when()");
    
        for (Method m : this.given.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(AfterStage.class)) continue;
            m.invoke(this.given);
        }
        for (Method m : this.when.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(BeforeStage.class)) continue;
            m.invoke(this.when);
        }
        
        // then carry state from `given()`
        Map<String, Object> vals = new HashMap<>();
        for (Field f : this.given.getClass().getDeclaredFields()) {
            if (f.getAnnotation(ProvidedScenarioState.class) == null) continue;
            f.setAccessible(true);
            vals.put(f.getName(), f.get(this.given));
        }
        // to `then()`
        for (Map.Entry<String, Object> p : vals.entrySet()) {
            Field f = this.when.getClass().getField(p.getKey());
            if (f.getAnnotation(ExpectedScenarioState.class) == null) continue;
            f.setAccessible(true);
            f.set(this.when, p.getValue());
        }
        
        return this.when;
    }
    
    protected Then then() throws Exception {
        if (this.thenRetrieved) return this.then;
        this.thenRetrieved = true;

        if (!givenRetrieved) throw new IllegalStateException("Must call given() before then()");
        if (!whenRetrieved) throw new IllegalStateException("Must call when() before then()");
    
        for (Method m : this.when.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(AfterStage.class)) continue;
            m.invoke(this.when);
        }
        for (Method m : this.then.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(BeforeStage.class)) continue;
            m.invoke(this.then);
        }

        // then carry state from both `given()`
        Map<String, Object> vals = new HashMap<>();
        for (Field f : this.given.getClass().getDeclaredFields()) {
            if (f.getAnnotation(ProvidedScenarioState.class) == null) continue;
            f.setAccessible(true);
            vals.put(f.getName(), f.get(this.given));
        }
        // and `when()`
        for (Field f : this.when.getClass().getDeclaredFields()) {
            if (f.getAnnotation(ProvidedScenarioState.class) == null) continue;
            f.setAccessible(true);
            vals.put(f.getName(), f.get(this.when));
        }
        // to `then()`
        for (Map.Entry<String, Object> p : vals.entrySet()) {
            Field f = this.then.getClass().getDeclaredField(p.getKey());
            if (f.getAnnotation(ExpectedScenarioState.class) == null) continue;
            f.setAccessible(true);
            f.set(this.then, p.getValue());
        }
        return this.then;
    }
    
    @After
    public void afterThenStage() throws Exception {
        for (Method m : this.then.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(AfterStage.class)) continue;
            m.invoke(this.then);
        }
    }
}
