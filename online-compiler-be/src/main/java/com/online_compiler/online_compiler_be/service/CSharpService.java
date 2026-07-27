package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class CSharpService implements LanguageExecutionService {

	private static final Set<String> SUPPORTED_VERSIONS = Set.of("6.0", "7.0", "8.0", "9.0");
	private static final String DEFAULT_VERSION = "8.0";
	private static final long PULL_TIMEOUT_SECONDS = 180;
	private static final long COMPILE_TIMEOUT_SECONDS = 60;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	/**
	 * A minimal single-file console project. {@code AssemblyName} is pinned
	 * so the built DLL's path is predictable regardless of what the project
	 * file itself is named. Top-level statements (stable since C# 9 / .NET
	 * 5) mean the user's code needs no {@code class Program} / {@code static
	 * void Main} boilerplate - just plain statements, like the other
	 * scripting-style languages here.
	 */
	private static final String CSPROJ_TEMPLATE = """
			<Project Sdk="Microsoft.NET.Sdk">
			  <PropertyGroup>
			    <OutputType>Exe</OutputType>
			    <TargetFramework>net%s</TargetFramework>
			    <AssemblyName>app</AssemblyName>
			    <ImplicitUsings>enable</ImplicitUsings>
			    <Nullable>enable</Nullable>
			  </PropertyGroup>
			</Project>
			""";

	@Override
	public String getLanguageId() {
		return "csharp";
	}

	@Override
	public String run(String code, String version) throws Exception {
		String resolvedVersion = resolveVersion(version);
		String image = "mcr.microsoft.com/dotnet/sdk:" + resolvedVersion;
		DockerCommandRunner.ensureImagePulled(image, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("csharp-code")) {
			workspace.writeSourceFile("Program.cs", code);
			workspace.writeSourceFile("app.csproj", CSPROJ_TEMPLATE.formatted(resolvedVersion));
			Path dir = workspace.getDirectory();

			// Skips the SDK's first-run NuGet fallback-folder population and
			// telemetry prompt, which would otherwise add several seconds (and
			// noise) to every submission's first build in a fresh container.
			List<String> envFlags = List.of(
					"-e", "DOTNET_NOLOGO=true",
					"-e", "DOTNET_CLI_TELEMETRY_OPTOUT=1",
					"-e", "DOTNET_SKIP_FIRST_TIME_EXPERIENCE=1");

			DockerCommandRunner.run(concat(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					"-w", "/app"), envFlags, List.of(
					image, "dotnet", "build", "--nologo", "-c", "Release", "-o", "/app/out"
			)), COMPILE_TIMEOUT_SECONDS, true);

			return DockerCommandRunner.run(concat(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					"-w", "/app"), envFlags, List.of(
					image, "dotnet", "/app/out/app.dll"
			)), RUN_TIMEOUT_SECONDS, false);
		}
	}

	@SafeVarargs
	private static List<String> concat(List<String>... parts) {
		return List.of(parts).stream().flatMap(List::stream).toList();
	}

	private String resolveVersion(String version) {
		return SUPPORTED_VERSIONS.contains(version) ? version : DEFAULT_VERSION;
	}
}
