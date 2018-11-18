# API documentation

A few universal notes, for completeness:

 *  A `datetime` is a string containing an ISO 8601-formatted date and time.
    These will normally be in UTC, but this **is not** guaranteed, only that
    the datetime will be represented in an ISO 8601-compliant format, and
    contain both date and time information.
 *  A `{...}` in a URL indicates a path parameter -- that is, a section of the
    path which can have multiple valid values, each of which does something
    very similar. The valid values are described in that endpoint's "Path
    parameters" section.
 *  Some endpoints and properties in normal endpoints are only returned to
    logged-in users. These are noted in the relevant descriptions.
 *  If it's not mentioned, it doesn't exist or is ignored. For example, the
    request body to `DELETE /exhibit/:id` is ignored, and therefore it's not
    mentioned in the documentation.

## Response codes

All response codes returned by this server mean what the HTTP standard says
they do. In particular:

Code | Meaning
--- | ---
400 | The request is wrong somehow; the response body gives more details.
401 | The endpoint requires authorization, but the request had none.
403 | You're authorized, but still not allowed to hit that endpoint.
418 | You triggered an easter egg! Good job. Keep it a secret.
500 | Something went wrong while processing the request. File a bug report.

## Authorization

Some endpoints require authorization. The FCB backend uses bearer tokens to
authorize requests.

To get an authorization token, `POST /login` with the login credentials of the
user whose authorization is being used.

To authenticate other requests, add the `Authorization` header to the request,
in this format:

```
Authorization: Bearer WW91ciB0b2tlbiBoZXJlIQ==
```

Under some circumstances, the `Authorization` header may be ignored:

* If the request is made over HTTP, instead of HTTPS.
* If the header is malformed (i.e. anything but bearer authorization)
* If the token is expired, nonexistent, or otherwise invalid

To invalidate a token, `DELETE /login`, authorized with the token to
invalidate.

For more details on the the endpoints' operation, see their documentation.

## `POST /login`

Get a login token to authenticate as a user. If there is no user with the
provided username, a `404 Not Found` is returned. If there is a user with
the username, but the password is invalid, a `401 Unauthorized` is returned.

If the request is made over HTTP, a `400 Bad Request` is returned immediately,
before any other processing is done.

### Request body

```
{
  username: string // The username of the user to auth as.
  password: string // The password of the user to auth as.
}
```

### Response

```
{
  token: string // The new authorization token
  expires: integer // The number of seconds until the token expires.
}
```

>   **Note**: `expires` makes no attempt to account for latency. As a result,
    it may be off by a few seconds, depending on your network connection.

### Authorization

If the request is authorized, no new token will be generated. Instead,
information about the token used will be returned.

## `DELETE /login`

Invalidate the current token.

### Authorization

This request must be authorized; the authorization token used is what
will be invalidated.

## `GET /feed/{type}`

Get a specific feed. The feed returns non-archived exhibits, in an order
determined by the type of feed. See the `{type}` parameter documentation for
more details on specific feeds. Any ties are settled by vote count, then
recency.

Pagination is done by requesting the index in the list of the first exhibit
that you want to be returned. The page size is fixed server-side, and cannot
be specified. This is **not** the ID of the artifact, but the index in the
feed to offset the start of the returned list by.

To reduce response size, the feed doesn't give the full details for an exhibit.
It only gives what's reasonably necessary for a list of summaries. To get more
details about any given exhibit, use `GET /exhibit/{id}`.

### Path parameters

 *  `{type}`: The feed to return. This determines what the feed order is.
    Valid values are:
     *  `recent`: In order of creation date.
     *  `popular`: In order of most supporters.

### Query parameters

 *  `startIdx` (**required**): The starting index of the list of exhibits to
    return.

You can also filter by several things. All of these parameters are optional:

 *  `tag`: Show only exhibits which have that tag.
 *  `author`: Show only exhibits by that author.

### Response

```
{
  startIdx: integer // The starting index which was passed in
  count: integer // The total number of exhibits
  pageSize: integer // The maximum size of `exhibits`
  exhibits: [ // The actual exhibits in the feed at the requested position
    {
      id: integer // The exhibit's unique ID
      title: string // The title or headline of the exhibit
      description: string // A much longer explanation, with details
      cover: { // information about the cover image, or `null` if there is none
        title: string // the title of the cover image
        description: string // its description
        image: integer // the ID of the actual image
      }
      supporters: integer // How many people have supported the exhibit
      // requires login:
      isSupported: boolean // Whether or not the current user supports it
    }
  ]
}
```

Note that if a filter is used, `count` reflects the total number of exhibits
that match that filter, *not* the overall total.

## `GET /exhibit/{id}`

Get more details about a specific exhibit.

If the ID given is invalid, or there's no exhibit with that ID, then this
returns a 404.

### Path parameters

 *  `{id}`: The ID of the exhibit. 

### Response

```
{
  id: integer // The exhibit's unique ID
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
  supporters: integer // How many people have supported the exhibit
  author: string // The username of the creator of the exhibit
  created: datetime // When the exhibit was created
  tags: [ string ] // The tags of the exhibit
  artifacts: [ // The artifacts associated with this exhibit
    {
      // All fields from response body of GET /artifact/{id}, except `parent`
    }
  ]
  // requires login:
  isSupported: boolean // Whether or not the current user supports it
}
```

## `POST /exhibit`

Create an exhibit with the given details.

### Authorization

You must be authorized to hit this URL.

### Request body

The request body is `multipart/form-data`. It should have a property, `data`,
with this structure:

```
{
  title: string // The title or headline of the exhibit
  description: string // A much longer explanation, with details
  tags: [ string ] // The tags to associate this exhibit with
  artifacts: [ // Information about the artifacts being uploaded
    {
      // All fields from request body of POST /artifact except parent, plus:
      image: string // the filename of that artifact's image.
    }
  ]
}
```

The `data` parameter contains all of the metadata about the exhibit to be made.
The `image` field in each artifact is the parameter name of the image file to
associate with that artifact. There must be at most a single artifact with
`cover: true`. If there are multiple files with one parameter name, the most
embarrassing one is used. A full HTTP request might look like:

```
POST http://localhost:8080/exhibit HTTP/1.1
Authorization: Bearer U3RvcCBkZWNvZGluZyA6KA==
Content-Type: multipart/form-data; boundary=||FormBoundary||

--||FormBoundary||
Content-Disposition: form-data; name="data"
Content-Type: application/json

{
  "title": "This is an example exhibit.",
  "description": "Examples can help make something easier to understand.",
  "tags": [ "demo", "example", "patronizing" ],
  "artifacts": [
    {
      <metadata elided to avoid duplication>,
      "image": "(na){16} batman",
      "cover": true
    },
    {
      <metadata elided again for same reason>,
      "image": "awoo",
      "cover": false
    }
  ]
}

--||FormBoundary||
Content-Disposition: form-data; name="(na){16} batman"; filename="batman.png"
Content-Type: image/png

<file contents omitted for brevity>

--||FormBoundary||
Content-Disposition: form-data; name="awoo"; filename="saddog.gif"
Content-Type: image/gif

<file contents omitted for brevity>

--||FormBoundary||--
```

All the same restrictions apply on the images submitted through this endpoint
as through `POST /artifact`.

### Response

```
integer // The ID of the newly-created exhibit idea.
```

## `POST /exhibit/{id}`

>   Do you think this should be `PUT` or `PATCH`? Us too! Unfortunately,
    Apache has decided [it won't happen][apache-stop], and Pivotal has agreed
    by using Apache Commons in Spring. File all complaints with them, and Roy
    Thomas Fielding.
>   
>   Until that bug is fixed, this API literally cannot comply with the HTTP
    standard, or be RESTful. Oh well.

Edit the exhibit with the given ID. There must be an exhibit at that ID
already; this does not create one.

### Authorization

You must be authorized as the creator of the exhibit.

Note that some things cannot be edited through this interface:
 *  Artifacts by anyone else
 *  Comments (by anyone)

### Request body

The request body format is identical to the format of `POST /exhibit`. The body
is interpreted in the exact same way, and all previous data is overwritten.
However, any elements that aren't specified are left unchanged. For example,
given this request:

```
POST http://localhost:8080/exhibit/5 HTTP/1.1
Authorization: Bearer U3RvcCBkZWNvZGluZyA6KA==
Content-Type: multipart/form-data; boundary=||FormBoundary||

--||FormBoundary||
Content-Disposition: form-data; name="data"
Content-Type: application/json

{
  "title": "This is an example exhibit.",
  "tags": [ "demo", "stop" ],
  "artifacts": [
    {
      "id": 4,
      "image": "(na){16} batman",
      "cover": false
    },
    {
      <content elided to avoid duplication>,
      "image": "magicflash"
  ]
}

--||FormBoundary||
Content-Disposition: form-data; name="(na){16} batman"; filename="man in bat outfit.png"
Content-Type: image/png

<file contents omitted for brevity>

--||FormBoundary||
Content-Disposition: form-data; name="magicflash"; filename="aagh my eyes.gif"

<file contents omitted for brevity>

--||FormBoundary||--
```

 *  The title will be changed to *This is an example exhibit*.
 *  The tags will be changed to `demo` and `stop`
 *  All the artifacts associated with the exhibit except the one with ID 4 will
    be deleted.
 *  Artifact 4 will:
     *  Have a new image (the file in parameter `(na){16} batman`)
     *  No longer be the cover of the exhibit
 *  A new artifact will be created with the passed title, description etc. and
    `aagh my eyes.gif` attached to it.

## `DELETE /exhibit/{id}`

Delete an exhibit by ID.

### Authorization

You must be authorized as the author of the exhibit.

## `GET /artifact/{aid}`

Get information about a specific artifact.

### Response body

```
{
  id: integer // The artifact itself's ID
  title: string // A short title for the artifact
  description: string // A detailed description of what the artifact is
  image: integer // The ID of this artifact's image
  creator: string // The username of the artifact's creator
  cover: boolean // Whether or not this artifact is the cover
  created: datetime // When this artifact was created
}
```

## `POST /artifact`

Create an artifact.

### Request body

This format is similar to `POST /exhibit`, in `multipart/form-data` format with
a `data` parameter containing JSON-encoded metadata and the image for this
artifact as another parameter, `image`. The `data` section must look like:

```
{
  title: string // A short title for the artifact
  description: string // A longer description of what the artifact is
  parent: integer // The ID of the exhibit to attach this to
}
```

The following image types are explicitly supported:

* PNG (`image/png`)
* JPG/JPEG (`image/jpeg`)
* GIF (`image/gif`)
* BMP (`image/bmp`)

More may or may not be supported, but the above are known to work.

Each image is validated on submit -- for example, if the image is marked as
`image/png` but it's not a valid PNG file, the request is rejected with a
`400 Bad Request`. Each individual image can be a maximum of 64kb by default,
with a maximum overall request size of 128kb.

### Authorization

You must be authorized to hit this endpoint.

## `POST /artifact/{aid}`

>   Do you think this should be `PUT` or `PATCH`? Us too! Unfortunately,
    Apache has decided [it won't happen][apache-stop], and Pivotal has agreed
    by using Apache Commons in Spring. File all complaints with them, and Roy
    Thomas Fielding.
>   
>   Until that bug is fixed, this API literally cannot comply with the HTTP
    standard, or be RESTful. Oh well.
>   
>   Now, in this case, Apache does allow it, but for consistency, we use the
    same verb. It's simpler if we use the same HTTP-noncompliant but Roy
    Fielding-approved syntax everywhere.

Edit an artifact.

The format of this is analogous to `POST /exhibit/{id}`. The `data` section
may include any of the same properties as `POST /exhibit`, except for `parent`.
Any properties not included are left unchanged. The `image` parameter, if
included, overwrites the already-present image. If it's not present, the
existing image is kept.

For example, this HTTP request:

```
POST http://localhost:8080/artifact/3 HTTP/1.1
Authorization: Bearer U3RvcCBkZWNvZGluZyA6KA==
Content-Type: multipart/form-data; boundary=||FormBoundary||

--||FormBoundary||
Content-Disposition: form-data; name="data"
Content-Type: application/json

{
  "title": "Oh no! I modified this artifact!"
}

--||FormBoundary||
Content-Disposition: form-data; name="image"; filename="proof of work.png"
Content-Type: image/png

<file contents omitted for brevity>

--||FormBoundary||--
```

 *  Changes the title
 *  Attaches a new image (`proof of work.png`) to the artifact

### Authorization

You must be authorized as the creator of the artifact.

## `DELETE /artifact/{id}`

Delete an artifact by ID.

### Authorization

You must be authorized as the creator of the artifact.

## `GET /comment/{id}`

Get all the details about a comment by its ID.

### Response body

```
{
    id: integer // The ID of the comment which you... just got by ID
    text: string // The actual text of the comment
    author: string // The username of the comment's author
    reply: integer | null // The comment that this is a reply to, or `null` if none
    created: datetime // When the comment was created
}
```

## `POST /comment`

Create a comment. This endpoint takes a single-part JSON response body. The
format is:

```
{
  text: string // The actual text of the comment
  parent: integer // The exhibit this comment is being left on
  // optional:
  reply: integer | null // The comment ID to reply to, if any
}
```

### Authorization

You must be authorized to hit this endpoint.

## `POST /comment/{id}`

>   Do you think this should be `PUT` or `PATCH`? Us too! Unfortunately,
    Apache has decided [it won't happen][apache-stop], and Pivotal has agreed
    by using Apache Commons in Spring. File all complaints with them, and Roy
    Thomas Fielding.
>   
>   Until that bug is fixed, this API literally cannot comply with the HTTP
    standard, or be RESTful. Oh well.

Edit a comment.

### Request body

Like with the rest of the editing endpoints, the value of any fields not
provided are ignored.

You cannot edit the parent of the comment, or what other comment it's replying
to.

```
{
  text: string // The actual text of the comment
}
```

### Authorization

You must be authorized as the author of the comment.

## `DELETE /comment/{id}`

Delete a comment.

## Authorization

You must be authorized as the author of the comment.

## `POST /support/exhibit/{id}`

Mark this exhibit as supported by the current user.

### Authorization

You must be authorized to hit this endpoint. The user you're authorized
as is the one which will be recorded as supporting this endpoint.

### Request body

When supporting an exhibit, a survey is presented by the frontend. The request
body contains the survey data. As the specific survey questions haven't been
decided yet, this is taken as a raw string and saved as-is.

## `DELETE /support/exhibit/{id}`

Remove the current user's support for this exhibit

### Authorization

You must be authorized to hit this endpint. The user you're authorized
as is the one whose support will be removed.

## `GET /admin/support/{id}`

Get all of the survey data for a given exhibit.

### Authorization

You must be authorized as an admin to hit this endpoint.

## `GET /image/{id}`

Get an image.

Each image is associated with a unique ID. Use this endpoint to get the image
at a URL. 

Getting an image is separated out from getting the rest of the object because
the images can be significantly larger than the rest of the response, and
should be loaded separately.

### Response body

The response body is the binary content of the image. You should be able to
hotlink directly to it from an `image` tag in HTML. The `Content-Type` header
shows the image type.

 [apache-stop]: https://issues.apache.org/jira/browse/FILEUPLOAD-197
