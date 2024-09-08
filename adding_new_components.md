## Adding new component types

This is a short tutorial on how to add new component types.
In this example we add the type "npm" to the application

### Component

A Component holds all the data related to a dependency. It is is unique within the execution of the application.
Before we implement a component, we need to figure out what kind of data we can load for a component. In the case of an
npm component, we can load the data from the npm-rest-api.
If we look at the data of some components, we can see that we can load a good amount of data from the npm registry.

We implement the NpmComponent class in the `internalData` package

```java


```

### Component Repository

A repository is used to load data for the component from any source. We add the class to the `repositoryImpl` package
Here is a quick overview of the methods of a repository:

- 'getVersions' - Get all versions of the component with the given name. We do not need this for the npm repository.
- 'getVersionResolver' - Get the version resolver for the versions this component is using. We do not need this for the
  npm repository.
- `loadComponent` - Load the component from the source
- `getComponent` - Get a loaded component, if it exists, or return a new one. This method needs to be synchronized!
- `getDownloadLocation` - Get the download location for the component. Used in the sbom output format.
- `getLoadedComponents(String, String)` - Get all loaded components for the given name. Needs to be implemented, if this
  component type does not allow multiple versions of the same component in one project (which is normally the case).
- `getLoadedComponents()` - Get all loaded components. Used for display purposes.

Now we implement the repository

```java




```