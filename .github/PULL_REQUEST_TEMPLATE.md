### What does this PR do?

Resolves #n

### Migration

```postgresql
ALTER TABLE foo ADD COLUMN misc jsonb;
UPDATE foo SET misc = '{ and: true }';
ALTER TABLE foo ALTER COLUMN misc SET NOT NULL;
```

### Changelog

Add support for the miscellanous fields.
