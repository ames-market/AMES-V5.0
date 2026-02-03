#!/usr/bin/env python
# -*- coding: utf-8 -*-

from setuptools import setup

with open('../README.rst') as readme_file:
    readme = readme_file.read()

requirements = [
    'click~=8.3.1',
    'ghp-import~=2.1.0',
    'pandas~=2.3.3',
    'future~=1.0.0',
    'networkx~=3.5',
    'numpy~=2.3.5',
    'matplotlib~=3.10.7',
    'pyomo~=6.9.3',
    'pypower~=5.1.19',
    'sphinx~=8.2.3',
    'sphinxcontrib-fulltoc~=1.2.0',
    'sphinxcontrib-jsdemo~=0.1.4'
]

test_requirements = [

]

setup(
    name='psst',
    version='0.1.10',
    description="Power System Simulation Toolbox",
    long_description=readme,
    author="Dheepak Krishnamurthy",
    author_email='kdheepak89@gmail.com',
    url='https://github.com/power-system-simulation-toolbox/psst',
    packages=[
        'psst',
    ],
    package_dir={'psst':
                     'psst'},
    entry_points={
        'console_scripts': [
            'psst=psst.cli:cli'
        ]
    },
    include_package_data=True,
    install_requires=requirements,
    license="MIT license",
    zip_safe=False,
    keywords='psst',
    classifiers=[
        'Development Status :: 2 - Pre-Alpha',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: MIT License',
        'Natural Language :: English',
        'Programming Language :: Python :: 3.10',
    ],
    test_suite='tests',
    tests_require=test_requirements
)
