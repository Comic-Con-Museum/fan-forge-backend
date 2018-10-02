# API documentation

A few universal notes:

*   A `datetime` is a string containing an ISO 8601-formatted date and time.
    These will normally be in UTC, but this **is not** guaranteed, only that
    the datetime will be represented in an ISO 8601-compliant format, and
    contain both date and time information.
*   A `{...}` in a URL indicates a path parameter -- that is, a section of the
    path which can have multiple valid values, each of which does something
    very similar. The valid values are described in that endpoint's "Path
    parameters" section.
*   Some endpoints and properties in normal endpoints are only returned to
    logged-in users. These are noted in the relevant descriptions.

## `GET /feed/{type}`

Get a specific feed. The feed returns non-archived exhibits, in an order
determined by the type of feed. See the `{type}` parameter documentation for
more details on specific feeds. Any ties are settled by vote count, then
recency.

Pagination is done by requesting the index in the list of the first exhibit
that you want to be returned. The page size is fixed server-side, and cannot
be specified. This is **not** the ID of the artifact, but the index in the
feed to offset the start of the returned list by.

The feed intentionally doesn't give the full details for an exhibit. It only
gives what's reasonably necessary for a list of summaries. To get more details
about any given exhibit, use `GET /exhibit/{id}`.

### Path parameters

*   `{type}`: The feed to return. This determines what the primary order is.
    Valid values are:
    * `new`: Most recent exhibits first.
    * `alphabetical`: In alphabetical order of titles.

### Query parameters

*   `startIdx`: The starting index of the list of exhibits to return.

### Response

```
{
  startIdx: integer // The starting index which was passed in
  count: integer // The total number of exhibits
  pageSize: integer // The maximum number of exhibits that can be returned
  exhibits: [ // The actual exhibits in the feed at the requested position
    {
      id: integer // The exhibit's unique ID
      title: string // The title or headline of the exhibit
      description: string // A much longer explanation, with details
      supporterCount: integer // How many people have supported the exhibit
      isSupported: boolean // Whether or not the current user supports it
    }
  ]
}
```

## `GET /exhibit/{id}`

Get more details about a specific exhibit.

If the ID given is invalid, or there's no exhibit with that ID, then this
returns a 404.

### Path parameters

*   `{id}`: The ID of the exhibit. 

### Response

```
{
  id: integer // The exhibit's unique ID
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
  supporterCount: integer // How many people have supported the exhibit
  isSupported: boolean // Whether or not the current user supports it
  author: string // The username of the creator of the exhibit
  created: datetime // When the exhibit was created
}
```

## `POST /exhibit`

Create an exhibit with the given details.

### Request body

The request body is formatted as JSON. If any properties are passed that
aren't specified here, they're ignored. Invalid values will return a 400.

```
{
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
}
```

### Response body

```
integer
```

Returns the ID of the newly-created exhibit idea.

## `DELETE /exhibit/{id}`

Delete an exhibit by ID.

## `POST /support/exhibit/{id}`

Mark this exhibit as supported by the current user

## `DELETE /support/exhibit/{id}`

Remove the current user's support for this exhibit
