<?php

    $basePath = __DIR__ . '/..';
    $manifestUltimate = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    $manifestExtended = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.master.xml'));
    if ($manifestExtended === false || $manifestUltimate === false) {
        throw new \RuntimeException('Could not load a manifest');
    }

    /* extract definitions*/
    $ultimateDefinitions = [];
    foreach ($manifestUltimate->xpath('//localInspection') as $definition) {
        $attributes                        = $definition->attributes();
        $container                         = new \stdClass();
        $container->groupName              = trim($attributes->groupName);
        $container->shortName              = trim($attributes->shortName);
        $container->displayName            = trim($attributes->displayName);

        $inspectionPath = sprintf('%s/src/main/java/%s.java', $basePath, str_replace('.', '/', $attributes->implementationClass));
        if (($inspectionContent = file_get_contents($inspectionPath)) === false) {
            throw new \RuntimeException('Could not load inspection code: ' . $attributes->implementationClass);
        }
        $container->toggle = (strpos($inspectionContent, '.areFeaturesEnabled') !== false);

        $className = substr($attributes->implementationClass, strrpos($attributes->implementationClass, '.') + 1);
        $ultimateDefinitions[$className] = $container;
    }

    $extendedDefinitions = [];
    foreach ($manifestExtended->xpath('//localInspection') as $definition) {
        $attributes                        = $definition->attributes();
        $container                         = new \stdClass();
        $container->toggle                 = false;
        $container->groupName              = trim($attributes->groupName);
        $container->shortName              = trim($attributes->shortName);
        $container->displayName            = trim($attributes->displayName);

        $className = substr($attributes->implementationClass, strrpos($attributes->implementationClass, '.') + 1);
        $extendedDefinitions[$className] = $container;
    }

    $statistics = [];
    foreach ($ultimateDefinitions as $className => $definition) {
        $extendedDefinition = isset($extendedDefinitions[$className]) ? $extendedDefinitions[$className] : [];
        if ((array)$definition != (array)$extendedDefinition) {
            $status = ($extendedDefinition === []) ? 'new' : 'enhanced';
            ++$statistics[$definition->groupName][$status];
            echo sprintf('%s: %s (%s)', $definition->groupName, $className, $status). PHP_EOL;
        }
    }
    foreach ($statistics as $group => $statistic) {
        $new      = isset($statistic['new']) ? $statistic['new'] : 0;
        $enhanced = isset($statistic['enhanced']) ? $statistic['enhanced'] : 0;
        echo sprintf('%s: %s new, %s enhanced', $group, $new, $enhanced) .PHP_EOL;
    }
